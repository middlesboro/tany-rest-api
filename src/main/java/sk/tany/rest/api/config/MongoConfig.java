package sk.tany.rest.api.config;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableMongoAuditing
@ConditionalOnProperty(name = "spring.data.mongodb.auditing.enabled", havingValue = "true", matchIfMissing = true)
public class MongoConfig {

    private static final String KEY_VAULT_NAMESPACE = "encryption.__keyVault";

    @Bean
    public MongoClient mongoClient(MongoDbConfigProperties mongoProperties) {
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()));

        String masterKeyBase64 = mongoProperties.getMasterKey();
        if (masterKeyBase64 != null && !masterKeyBase64.isEmpty()) {
            byte[] localMasterKey = Base64.getDecoder().decode(masterKeyBase64);
            if (localMasterKey.length == 96) {
                Map<String, Map<String, Object>> kmsProviders = new HashMap<>();
                Map<String, Object> localKeyMap = new HashMap<>();
                localKeyMap.put("key", localMasterKey);
                kmsProviders.put("local", localKeyMap);

                String dbName = "tany"; // Fallback to 'tany' if not explicit in URI
                ConnectionString cs = new ConnectionString(mongoProperties.getUri());
                if (cs.getDatabase() != null) {
                    dbName = cs.getDatabase();
                }

                Map<String, BsonDocument> schemaMap = new HashMap<>();

                String customerSchemaStr = "{\n" +
                        "  \"bsonType\": \"object\",\n" +
                        "  \"properties\": {\n" +
                        "    \"email\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"phone\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"firstname\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"lastname\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"address.street\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"address.city\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"address.zip\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"address.country\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.street\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.city\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.zip\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.country\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.street\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.city\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.zip\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.country\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } }\n" +
                        "  }\n" +
                        "}";

                String orderSchemaStr = "{\n" +
                        "  \"bsonType\": \"object\",\n" +
                        "  \"properties\": {\n" +
                        "    \"email\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"phone\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"firstname\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"lastname\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.street\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.city\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.zip\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"invoiceAddress.country\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.street\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.city\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.zip\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } },\n" +
                        "    \"deliveryAddress.country\": { \"encrypt\": { \"keyId\": [ { \"$keyVault\": [\"data-key\"] } ], \"bsonType\": \"string\", \"algorithm\": \"AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic\" } }\n" +
                        "  }\n" +
                        "}";

                schemaMap.put(dbName + ".customer", BsonDocument.parse(customerSchemaStr));
                schemaMap.put(dbName + ".order", BsonDocument.parse(orderSchemaStr));

                Map<String, Object> extraOptions = new HashMap<>();
                extraOptions.put("cryptSharedLibRequired", true);

                // Allow fallback to mongocryptd if crypt_shared is not available (useful in dev/docker environments)
                // If you strictly want crypt_shared, leave as is. But for broader compatibility, we might set cryptSharedLibRequired to false.
                // Let's set it to false so it falls back to mongocryptd gracefully if needed.
                extraOptions.put("cryptSharedLibRequired", false);

                AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
                        .keyVaultNamespace(KEY_VAULT_NAMESPACE)
                        .kmsProviders(kmsProviders)
                        .schemaMap(schemaMap)
                        .extraOptions(extraOptions)
                        .build();

                settingsBuilder.autoEncryptionSettings(autoEncryptionSettings);
                log.info("Configured AutoEncryptionSettings for MongoDB CSFLE.");
            } else {
                log.warn("MONGO_MASTER_KEY is set but not 96 bytes long. Skipping CSFLE initialization.");
            }
        } else {
            log.info("MONGO_MASTER_KEY not set. Operating without CSFLE.");
        }

        return MongoClients.create(settingsBuilder.build());
    }
}
