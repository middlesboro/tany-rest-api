package sk.tany.rest.api.service.feed;

import java.io.File;

public interface HeurekaFeedService {
    void generateProductFeed();
    void generateAvailabilityFeed();
    File getProductFeedFile();
    File getAvailabilityFeedFile();
}
