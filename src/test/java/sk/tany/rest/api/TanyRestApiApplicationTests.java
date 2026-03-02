package sk.tany.rest.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires running MongoDB")
@SpringBootTest(properties = {
    "spring.mongodb.port=0",
    "de.flapdoodle.mongodb.embedded.version=6.0.11",
    "besteron.api-key=test"
})
class TanyRestApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
