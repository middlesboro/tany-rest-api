package sk.tany.rest.api.component;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.product.ProductRepository;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SlugGenerator {

    private final ProductRepository productRepository;
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public String generateSlug(String input, String excludeId) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException("Input for slug generation cannot be empty");
        }

        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        // Remove accents and special chars
        String slug = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("-+", "-");
        slug = slug.replaceAll("^-|-$", "");

        if (StringUtils.isBlank(slug)) {
             // Fallback if slug becomes empty (e.g. input was "???")
             slug = "product";
        }

        String originalSlug = slug;
        int counter = 1;
        while (productRepository.existsBySlug(slug, excludeId)) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
