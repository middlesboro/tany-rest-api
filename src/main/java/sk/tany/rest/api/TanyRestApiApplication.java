package sk.tany.rest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;
import sk.tany.rest.api.config.ApiRuntimeHints;

@ImportRuntimeHints(ApiRuntimeHints.class)
@EnableScheduling
@SpringBootApplication
public class TanyRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TanyRestApiApplication.class, args);
    }

}
