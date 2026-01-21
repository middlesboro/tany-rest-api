package sk.tany.rest.api.domain.category;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.dizitart.no2.Nitrite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class CategoryRepository extends AbstractInMemoryRepository<Category> {

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private static final int MAX_EDIT_DISTANCE = 2;

    private final Map<String, List<String>> childrenCache = new ConcurrentHashMap<>();

    public CategoryRepository(Nitrite nitrite) {
        super(nitrite, Category.class);
    }

    @Override
    public void init() {
        super.init();
        refreshChildrenCache();
    }

    @Override
    public Category save(Category entity) {
        Category saved = super.save(entity);
        refreshChildrenCache();
        return saved;
    }

    @Override
    public void saveAll(Iterable<Category> entities) {
        super.saveAll(entities);
        refreshChildrenCache();
    }

    @Override
    public void deleteById(String id) {
        super.deleteById(id);
        refreshChildrenCache();
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
        refreshChildrenCache();
    }

    private void refreshChildrenCache() {
        childrenCache.clear();
        for (Category category : memoryCache.values()) {
            if (category.getParentId() != null) {
                childrenCache.computeIfAbsent(category.getParentId(), k -> new ArrayList<>()).add(category.getId());
            }
        }
    }

    public List<String> getChildrenIds(String parentId) {
        return childrenCache.getOrDefault(parentId, Collections.emptyList());
    }

    public Optional<Category> findByPrestashopId(Long prestashopId) {
        return memoryCache.values().stream()
                .filter(c -> c.getPrestashopId() != null && c.getPrestashopId().equals(prestashopId))
                .findFirst();
    }

    public Page<Category> searchCategories(String query, Pageable pageable) {
        String normalizedQuery = null;
        String[] queryWords = null;

        if (StringUtils.isNotBlank(query)) {
            normalizedQuery = StringUtils.stripAccents(query.toLowerCase()).trim();
            queryWords = normalizedQuery.split("\\s+");
        }

        final String finalNormalizedQuery = normalizedQuery;
        final String[] finalQueryWords = queryWords;

        List<Category> filteredCategories = memoryCache.values().stream()
                .filter(c -> {
                    if (finalQueryWords != null) {
                        if (c.getTitle() == null) {
                            return false;
                        }
                        String normalizedName = StringUtils.stripAccents(c.getTitle().toLowerCase());
                        String[] nameWords = normalizedName.split("\\s+");

                        return Arrays.stream(finalQueryWords).allMatch(qWord ->
                                Arrays.stream(nameWords).anyMatch(nWord ->
                                        nWord.contains(qWord) || levenshtein.apply(nWord, qWord) <= MAX_EDIT_DISTANCE
                                )
                        );
                    }
                    return true;
                })
                .sorted((c1, c2) -> {
                    if (finalNormalizedQuery != null) {
                        Double score1 = calculateRelevance(c1.getTitle(), finalNormalizedQuery);
                        Double score2 = calculateRelevance(c2.getTitle(), finalNormalizedQuery);
                        return score2.compareTo(score1);
                    }
                    return StringUtils.compareIgnoreCase(c1.getTitle(), c2.getTitle());
                })
                .toList();

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filteredCategories, pageable, filteredCategories.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCategories.size());

        if (start > filteredCategories.size()) {
            return new PageImpl<>(List.of(), pageable, filteredCategories.size());
        }

        List<Category> pageContent = filteredCategories.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredCategories.size());
    }

    private Double calculateRelevance(String text, String normalizedQuery) {
        if (text == null) {
            return 0.0;
        }
        String normalizedName = StringUtils.stripAccents(text.toLowerCase());
        return jaroWinkler.apply(normalizedName, normalizedQuery);
    }
}
