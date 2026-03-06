package sk.tany.rest.api.config;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionKeyInitializer {

    private final MongoDbConfigProperties mongoProperties;

    private static final String KEY_VAULT_NAMESPACE = "encryption.__keyVault";
    private static final String KEY_ALT_NAME = "data-key";

    @Getter
    private BsonBinary dataKeyId;

    @PostConstruct
    public void init() {
        String masterKeyBase64 = mongoProperties.getMasterKey();
        if (masterKeyBase64 == null || masterKeyBase64.isEmpty()) {
            log.warn("MONGO_MASTER_KEY not set. Skipping CSFLE initialization.");
            return;
        }

        byte[] localMasterKey = Base64.getDecoder().decode(masterKeyBase64);
        if (localMasterKey.length != 96) {
            throw new IllegalArgumentException("MONGO_MASTER_KEY must be 96 bytes.");
        }

        Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
        kmsProviders.put("local", Map.of("key", localMasterKey));

        String[] parts = KEY_VAULT_NAMESPACE.split("\\.");
        String dbName = parts[0];
        String collName = parts[1];

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .build();

        try (MongoClient plainClient = MongoClients.create(clientSettings)) {

            plainClient.getDatabase(dbName)
                    .getCollection(collName)
                    .createIndex(
                            new Document("keyAltNames", 1),
                            new IndexOptions()
                                    .unique(true)
                                    .partialFilterExpression(new Document("keyAltNames",
                                            new Document("$exists", true)))
                    );

            ClientEncryptionSettings ces = ClientEncryptionSettings.builder()
                    .keyVaultMongoClientSettings(clientSettings)
                    .keyVaultNamespace(KEY_VAULT_NAMESPACE)
                    .kmsProviders(kmsProviders)
                    .build();

            try (ClientEncryption clientEncryption = ClientEncryptions.create(ces)) {
                BsonDocument existing = clientEncryption.getKeyByAltName(KEY_ALT_NAME);

                if (existing != null) {
                    dataKeyId = existing.getBinary("_id");
                    log.info("Loaded existing DEK: {}", dataKeyId);
                } else {
                    dataKeyId = clientEncryption.createDataKey("local",
                            new DataKeyOptions().keyAltNames(List.of(KEY_ALT_NAME)));
                    log.info("Created new DEK: {}", dataKeyId);
                }
            }
        }
    }
}
