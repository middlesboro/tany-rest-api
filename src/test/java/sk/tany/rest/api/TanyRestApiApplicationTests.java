package sk.tany.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import sk.tany.rest.api.config.MockRepositoriesConfig;

@SpringBootTest(properties = {
    "spring.data.mongodb.port=0",
    "de.flapdoodle.mongodb.embedded.version=6.0.11"
})
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class
})
@Import(MockRepositoriesConfig.class)
class TanyRestApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
