package sk.tany.rest.api.domain.pagecontent;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class PageContentRepository extends AbstractInMemoryRepository<PageContent> {

    public PageContentRepository(Nitrite nitrite) {
        super(nitrite, PageContent.class);
    }

    public Optional<PageContent> findBySlug(String slug) {
        return memoryCache.values().stream()
                .filter(pc -> pc.getSlug() != null && pc.getSlug().equals(slug))
                .findFirst();
    }
}
