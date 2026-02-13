package sk.tany.rest.api.domain.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByPrestashopId(Long prestashopId);

    Optional<Category> findBySlug(String slug);

    // searchCategories logic was custom. In Mongo we can use regex or text search.
    // For now, let's assume ProductSearchEngine handles complex search or we use a simple regex.
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Page<Category> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
