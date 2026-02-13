package sk.tany.rest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TanyRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TanyRestApiApplication.class, args);
    }

}
