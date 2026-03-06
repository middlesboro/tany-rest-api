package sk.tany.rest.api.config;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableMongoAuditing
@ConditionalOnProperty(name = "spring.data.mongodb.auditing.enabled", havingValue = "true", matchIfMissing = true)
public class MongoConfig {

    private static final String KEY_VAULT_NAMESPACE = "encryption.__keyVault";
    private static final String DETERMINISTIC = "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic";

    @Bean
    public MongoClient mongoClient(MongoDbConfigProperties mongoProperties,
                                   EncryptionKeyInitializer keyInitializer) {

        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()));

        String masterKeyBase64 = mongoProperties.getMasterKey();
        BsonBinary keyId = keyInitializer.getDataKeyId();

        if (masterKeyBase64 != null && !masterKeyBase64.isEmpty() && keyId != null) {
            byte[] localMasterKey = Base64.getDecoder().decode(masterKeyBase64);
            if (localMasterKey.length == 96) {
                Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
                kmsProviders.put("local", Map.of("key", localMasterKey));

                String dbName = mongoProperties.getDatabase();
                ConnectionString cs = new ConnectionString(mongoProperties.getUri());
                if (cs.getDatabase() != null) {
                    dbName = cs.getDatabase();
                }

                Map<String, BsonDocument> schemaMap = new HashMap<>();
                schemaMap.put(dbName + ".customer", buildEncryptedSchema(keyId));
                schemaMap.put(dbName + ".cart", buildEncryptedSchema(keyId));
                schemaMap.put(dbName + ".order", buildEncryptedSchema(keyId));

                Map<String, Object> extraOptions = new HashMap<>();
                extraOptions.put("cryptSharedLibPath", mongoProperties.getCryptLibPath());
                extraOptions.put("cryptSharedLibRequired", true);

                AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
                        .keyVaultNamespace(KEY_VAULT_NAMESPACE)
                        .kmsProviders(kmsProviders)
                        .schemaMap(schemaMap)
                        .extraOptions(extraOptions)
                        .build();

                settingsBuilder.autoEncryptionSettings(autoEncryptionSettings);
                log.info("Configured AutoEncryptionSettings for MongoDB CSFLE.");
            } else {
                log.warn("MONGO_MASTER_KEY is not 96 bytes. Skipping CSFLE.");
            }
        } else {
            log.info("MONGO_MASTER_KEY not set or DEK not initialized. Operating without CSFLE.");
        }

        return MongoClients.create(settingsBuilder.build());
    }

    private BsonDocument encryptedField(BsonBinary keyId) {
        return new BsonDocument("encrypt", new BsonDocument()
                .append("bsonType", new BsonString("string"))
                .append("algorithm", new BsonString(DETERMINISTIC))
                .append("keyId", new BsonArray(List.of(keyId)))
        );
    }

    private BsonDocument buildEncryptedSchema(BsonBinary keyId) {
        return new BsonDocument("bsonType", new BsonString("object"))
                .append("properties", new BsonDocument()
                        .append("email",        encryptedField(keyId))
                        .append("phone",        encryptedField(keyId))
                        .append("firstname",    encryptedField(keyId))
                        .append("lastname",     encryptedField(keyId))
                        .append("invoiceAddress",   nestedAddressSchema(keyId))
                        .append("deliveryAddress",  nestedAddressSchema(keyId))
                );
    }

    private BsonDocument nestedAddressSchema(BsonBinary keyId) {
        return new BsonDocument("bsonType", new BsonString("object"))
                .append("properties", new BsonDocument()
                        .append("street",   encryptedField(keyId))
                        .append("city",     encryptedField(keyId))
                        .append("zip",      encryptedField(keyId))
                        .append("country",  encryptedField(keyId))
                );
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, MongoDbConfigProperties mongoProperties) {
        String dbName = mongoProperties.getDatabase();
        return new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

}
