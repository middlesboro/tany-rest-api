package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.service.feed.GoogleFeedService;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleFeedScheduler {

    private final GoogleFeedService googleFeedService;

    @Scheduled(cron = "0 30 11 * * *")
    public void generateProductFeed() {
        log.info("Scheduled task: generating Google product feed.");
        googleFeedService.generateProductFeed();
    }
}
