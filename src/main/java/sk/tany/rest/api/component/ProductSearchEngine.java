package sk.tany.rest.api.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchEngine {

    private final ProductRepository productRepository;
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private final List<Product> cachedProducts = new CopyOnWriteArrayList<>();

    private static final int MAX_EDIT_DISTANCE = 2;

    @EventListener(ApplicationReadyEvent.class)
    public void loadProducts() {
        log.info("Loading products into search engine...");
        cachedProducts.clear();
        cachedProducts.addAll(productRepository.findAll());
        log.info("Loaded {} products into search engine.", cachedProducts.size());
    }

    public List<Product> searchAndSort(String query) {
        if (StringUtils.isBlank(query)) {
            return List.of();
        }

        // 1. Normalizácia vstupu
        String normalizedQuery = StringUtils.stripAccents(query.toLowerCase()).trim();
        String[] queryWords = normalizedQuery.split("\\s+");

        return cachedProducts.stream()
            // 2. Filtrovanie (musí obsahovať aspoň niečo podobné všetkým slovám v dopyte)
            .filter(product -> {
                if (product.getTitle() == null) {
                    return false;
                }
                String normalizedName = StringUtils.stripAccents(product.getTitle().toLowerCase());
                String[] nameWords = normalizedName.split("\\s+");

                return Arrays.stream(queryWords).allMatch(qWord ->
                    Arrays.stream(nameWords).anyMatch(nWord ->
                        nWord.contains(qWord) || levenshtein.apply(nWord, qWord) <= MAX_EDIT_DISTANCE
                    )
                );
            })
            // 3. Výpočet skóre a zoradenie
            .sorted((p1, p2) -> {
                Double score1 = calculateRelevance(p1.getTitle(), normalizedQuery);
                Double score2 = calculateRelevance(p2.getTitle(), normalizedQuery);
                return score2.compareTo(score1); // Od najvyššieho skóre
            })
            .collect(Collectors.toList());
    }

    private Double calculateRelevance(String productName, String normalizedQuery) {
        if (productName == null) {
            return 0.0;
        }
        String normalizedName = StringUtils.stripAccents(productName.toLowerCase());
        // Jaro-Winkler vráti hodnotu medzi 0.0 a 1.0
        return jaroWinkler.apply(normalizedName, normalizedQuery);
    }
}
