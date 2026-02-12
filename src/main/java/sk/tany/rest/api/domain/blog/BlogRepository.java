package sk.tany.rest.api.domain.blog;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Comparator;
import java.util.List;

@Repository
public class BlogRepository extends AbstractInMemoryRepository<Blog> {

    public BlogRepository(Nitrite nitrite) {
        super(nitrite, Blog.class);
    }

    public List<Blog> findAllByVisibleTrue() {
        return memoryCache.values().stream()
                .filter(Blog::isVisible)
                .sorted(Comparator.comparingInt(Blog::getOrder))
                .toList();
    }
}
