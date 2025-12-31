package sk.tany.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=secret123456789012345678901234567890",
        "eshop.base-url=http://localhost:8080",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test",
        "imagekit.url-endpoint=url",
        "imagekit.public-key=pub",
        "imagekit.private-key=priv",
        "mailersend.api-token=token"
})
class TanyRestApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
