package sk.tany.rest.api.domain.blog;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class BlogRepository extends AbstractInMemoryRepository<Blog> {

    public BlogRepository(Nitrite nitrite) {
        super(nitrite, Blog.class);
    }

    public Optional<Blog> findBySlug(String slug) {
        return memoryCache.values().stream()
                .filter(blog -> slug.equals(blog.getSlug()))
                .findFirst();
    }

    public List<Blog> findAllByVisibleTrue() {
        return memoryCache.values().stream()
                .filter(Blog::isVisible)
                .sorted(Comparator.comparingInt(Blog::getOrder))
                .toList();
    }
}
