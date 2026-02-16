package sk.tany.rest.api.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findByProductIdentifier(Long productIdentifier);

    List<Product> findAllByBrandId(String brandId);

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

    java.util.stream.Stream<Product> streamAllByActiveTrue();

    @Query(value = "{}", sort = "{ 'productIdentifier' : -1 }")
    List<Product> findTopByOrderByProductIdentifierDesc(Pageable pageable);

    default Long findMaxProductIdentifier() {
        List<Product> top = findTopByOrderByProductIdentifierDesc(Pageable.ofSize(1));
        return top.isEmpty() || top.get(0).getProductIdentifier() == null ? 0L : top.get(0).getProductIdentifier();
    }

    List<Product> findAllByProductFilterParametersFilterParameterValueId(String filterParameterValueId);

    // This method was used for complex category filtering, now delegated to ProductSearchEngine?
    // But if we want simple Mongo query:
    // Page<Product> findByCategoryIdsIn(Collection<String> categoryIds, Pageable pageable);
    // But callers might pass just one ID and expect hierarchy support.
    // If so, they should use ProductSearchEngine or we need to lookup subcategories first.
}
