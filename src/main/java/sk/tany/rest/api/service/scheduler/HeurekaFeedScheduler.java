package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.service.feed.HeurekaFeedService;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeurekaFeedScheduler {

    private final HeurekaFeedService heurekaFeedService;

    @Scheduled(cron = "0 30 11 * * *")
    public void generateProductFeed() {
        log.info("Scheduled task: generating Heureka product feed.");
        heurekaFeedService.generateProductFeed();
    }

    @Scheduled(cron = "0 0 */2 * * *")
    public void generateAvailabilityFeed() {
        log.info("Scheduled task: generating Heureka availability feed.");
        heurekaFeedService.generateAvailabilityFeed();
    }
}
