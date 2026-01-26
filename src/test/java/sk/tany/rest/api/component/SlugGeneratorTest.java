package sk.tany.rest.api.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.product.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlugGeneratorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SlugGenerator slugGenerator;

    @Test
    void generateSlug_ShouldGenerateSimpleSlug() {
        when(productRepository.existsBySlug(eq("my-product"), any())).thenReturn(false);
        String slug = slugGenerator.generateSlug("My Product", null);
        assertEquals("my-product", slug);
    }

    @Test
    void generateSlug_ShouldHandleSpecialChars() {
        when(productRepository.existsBySlug(eq("my-product-more"), any())).thenReturn(false);
        String slug = slugGenerator.generateSlug("My Product & More!", null);
        assertEquals("my-product-more", slug);
    }

    @Test
    void generateSlug_ShouldHandleDiacritics() {
        when(productRepository.existsBySlug(eq("cucoriedkovy-dzem"), any())).thenReturn(false);
        String slug = slugGenerator.generateSlug("Čučoriedkový džem", null);
        assertEquals("cucoriedkovy-dzem", slug);
    }

    @Test
    void generateSlug_ShouldAppendCounterIfSlugExists() {
        when(productRepository.existsBySlug(eq("product-1"), any())).thenReturn(true);
        when(productRepository.existsBySlug(eq("product-1-1"), any())).thenReturn(false);

        String slug = slugGenerator.generateSlug("Product 1", null);
        assertEquals("product-1-1", slug);
    }
}
