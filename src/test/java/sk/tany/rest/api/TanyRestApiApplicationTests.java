package sk.tany.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import sk.tany.rest.api.config.MockRepositoriesConfig;

@SpringBootTest(properties = {
    "spring.mongodb.port=0",
    "de.flapdoodle.mongodb.embedded.version=6.0.11"
})
@Import(MockRepositoriesConfig.class)
class TanyRestApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
