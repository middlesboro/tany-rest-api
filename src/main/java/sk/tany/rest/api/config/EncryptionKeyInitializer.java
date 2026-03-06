package sk.tany.rest.api.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.client.vault.ClientEncryption;
import org.bson.BsonBinary;
import org.bson.Document;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionKeyInitializer implements ApplicationRunner {

    private final MongoDbConfigProperties mongoProperties;

    private static final String KEY_VAULT_NAMESPACE = "encryption.__keyVault";
    private static final String KEY_ALT_NAME = "data-key";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String masterKeyBase64 = mongoProperties.getMasterKey();
        if (masterKeyBase64 == null || masterKeyBase64.isEmpty()) {
            log.warn("MONGO_MASTER_KEY environment variable is not set. Skipping CSFLE initialization.");
            return;
        }

        byte[] localMasterKey = Base64.getDecoder().decode(masterKeyBase64);
        if (localMasterKey.length != 96) {
            throw new IllegalArgumentException("MONGO_MASTER_KEY must be a 96-byte base64 encoded string.");
        }

        Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
        Map<String, Object> localKeyMap = new HashMap<>();
        localKeyMap.put("key", localMasterKey);
        kmsProviders.put("local", localKeyMap);

        String[] namespaceParts = KEY_VAULT_NAMESPACE.split("\\.");
        String dbName = namespaceParts[0];
        String collName = namespaceParts[1];

        // 1. Create a MongoClient to configure the KeyVault collection
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .build();

        try (MongoClient mongoClient = MongoClients.create(clientSettings)) {

            // 2. Ensure the KeyVault collection has a unique index on keyAltNames
            mongoClient.getDatabase(dbName)
                    .getCollection(collName)
                    .createIndex(new Document("keyAltNames", 1),
                            new IndexOptions().unique(true).partialFilterExpression(new Document("keyAltNames", new Document("$exists", true))));

            // 3. Create ClientEncryption
            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                    .keyVaultMongoClientSettings(clientSettings)
                    .keyVaultNamespace(KEY_VAULT_NAMESPACE)
                    .kmsProviders(kmsProviders)
                    .build();

            try (ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings)) {

                // 4. Check if the key already exists
                BsonBinary dataKeyId = null;
                for (Document doc : mongoClient.getDatabase(dbName).getCollection(collName).find()) {
                    if (doc.containsKey("keyAltNames")) {
                        for (Object altName : doc.getList("keyAltNames", String.class)) {
                            if (KEY_ALT_NAME.equals(altName)) {
                                dataKeyId = new BsonBinary(doc.get("_id", org.bson.types.Binary.class).getType(), doc.get("_id", org.bson.types.Binary.class).getData());
                                break;
                            }
                        }
                    }
                    if (dataKeyId != null) {
                        break;
                    }
                }

                if (dataKeyId == null) {
                    // 5. Create a new Data Encryption Key
                    DataKeyOptions dataKeyOptions = new DataKeyOptions().keyAltNames(Collections.singletonList(KEY_ALT_NAME));
                    BsonBinary newKeyId = clientEncryption.createDataKey("local", dataKeyOptions);
                    log.info("Created new Data Encryption Key with keyAltName: {}", KEY_ALT_NAME);
                } else {
                    log.info("Found existing Data Encryption Key with keyAltName: {}", KEY_ALT_NAME);
                }
            }
        }
    }
}