package sk.tany.rest.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractInMemoryRepositoryTest {

    private TestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestRepository(Mockito.mock(Nitrite.class));
        repository.save(new TestEntity("1", "A", 10));
        repository.save(new TestEntity("2", "C", 5));
        repository.save(new TestEntity("3", "B", 20));
    }

    @Test
    void findAll_ShouldReturnSortedPage_WhenSortIsProvided() {
        // Sort by name ASC
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<TestEntity> result = repository.findAll(pageable);

        assertThat(result.getContent()).extracting(TestEntity::getName)
                .containsExactly("A", "B", "C");

        // Sort by value DESC
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "value"));
        result = repository.findAll(pageable);

        assertThat(result.getContent()).extracting(TestEntity::getValue)
                .containsExactly(20, 10, 5);
    }

    @Test
    void findAll_ShouldReturnSortedPage_WhenMultipleSortIsProvided() {
         repository.save(new TestEntity("4", "A", 30));

         // Sort by name ASC, then value DESC
         Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name"), Sort.Order.desc("value")));
         Page<TestEntity> result = repository.findAll(pageable);

         assertThat(result.getContent()).extracting(TestEntity::getName)
                 .containsExactly("A", "A", "B", "C");

         assertThat(result.getContent()).extracting(TestEntity::getValue)
                 .containsExactly(30, 10, 20, 5);
    }

    @Test
    void findAll_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));
        Page<TestEntity> result = repository.findAll(pageable);
        assertThat(result.getContent()).extracting(TestEntity::getName).containsExactly("A", "B");
        assertThat(result.getTotalElements()).isEqualTo(3);

        pageable = PageRequest.of(1, 2, Sort.by("name"));
        result = repository.findAll(pageable);
        assertThat(result.getContent()).extracting(TestEntity::getName).containsExactly("C");
    }

    // -- Test Support Classes --

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestEntity {
        private String id;
        private String name;
        private Integer value;
    }

    static class TestRepository extends AbstractInMemoryRepository<TestEntity> {
        public TestRepository(Nitrite nitrite) {
            super(nitrite, TestEntity.class);
            // manually init repo/cache for test since we mock Nitrite
            // Override save to skip Nitrite interactions
        }

        @Override
        public void init() {
            // do nothing
        }

        @Override
        public TestEntity save(TestEntity entity) {
            memoryCache.put(entity.getId(), entity);
            return entity;
        }
    }
}
