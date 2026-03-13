package sk.tany.rest.api.service.feed;

import java.io.File;

public interface GoogleFeedService {
    void generateProductFeed();
    File getProductFeedFile();
}
