package sk.tany.rest.api.domain.review;

import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReviewRepositoryTest {

    @Mock
    private Nitrite nitrite;

    private ReviewRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ReviewRepository(nitrite);
    }

    @Test
    void findAllByProductId_shouldSortResults() throws Exception {
        // Arrange
        Review r1 = new Review(); r1.setId("1"); r1.setProductId("p1"); r1.setRating(1);
        Review r2 = new Review(); r2.setId("2"); r2.setProductId("p1"); r2.setRating(5);
        Review r3 = new Review(); r3.setId("3"); r3.setProductId("p1"); r3.setRating(3);

        injectReview("1", r1);
        injectReview("2", r2);
        injectReview("3", r3);

        Sort sort = Sort.by(Sort.Direction.DESC, "rating");

        // Act
        // This method is expected to be implemented in ReviewRepository
        List<Review> result = repository.findAllByProductId("p1", sort);

        // Assert
        assertEquals(3, result.size());
        assertEquals(5, result.getFirst().getRating());
        assertEquals(3, result.get(1).getRating());
        assertEquals(1, result.get(2).getRating());
    }

    @Test
    void existsDuplicate_shouldReturnTrue_whenDuplicateExists() throws Exception {
        Review review = new Review();
        review.setId("1");
        review.setCustomerId("123");
        review.setCustomerName("John");
        review.setTitle("Great");
        review.setText("Good product");
        injectReview("1", review);

        boolean exists = repository.existsDuplicate("123", "John", "Great", "Good product");
        assertEquals(true, exists);
    }

    @Test
    void existsDuplicate_shouldReturnFalse_whenNoDuplicate() throws Exception {
        Review review = new Review();
        review.setId("1");
        review.setCustomerId("123");
        review.setCustomerName("John");
        review.setTitle("Great");
        review.setText("Good product");
        injectReview("1", review);

        boolean exists = repository.existsDuplicate("124", "John", "Great", "Good product");
        assertEquals(false, exists);
    }

    @SuppressWarnings("unchecked")
    private void injectReview(String id, Review review) throws Exception {
        Field cacheField = AbstractInMemoryRepository.class.getDeclaredField("memoryCache");
        cacheField.setAccessible(true);
        Map<String, Review> cache = (Map<String, Review>) cacheField.get(repository);
        cache.put(id, review);
    }
}
