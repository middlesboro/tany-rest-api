package sk.tany.rest.api.service.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.CrossSellProductDto;
import sk.tany.rest.api.dto.CrossSellProductsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrossSellAssistantJavaImplTest {

    @Mock
    private CrossSellTools crossSellTools;

    @InjectMocks
    private CrossSellAssistantJavaImpl crossSellAssistant;

    @Test
    void findCrossSellProducts_shouldHandleNullTitle() {
        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts(null, null);
        assertThat(response.getProducts()).isNotNull();
        assertThat(response.getProducts()).isEmpty();
    }

    @Test
    void findCrossSellProducts_shouldHandleHennaRule() {
        // Rule 1: "henna" -> "prirodny sampon", "sampon", "esencialny"
        mockSearch("prirodny sampon", createProduct("p1", "Sampon 1"));
        mockSearch("sampon", createProduct("p2", "Sampon 2"));
        mockSearch("esencialny", createProduct("p3", "Esencialny olej"));

        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts("Henna farba", Collections.emptyList());

        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.getProducts().get(0).getId()).isEqualTo("p1");
        assertThat(response.getProducts().get(1).getId()).isEqualTo("p2");
        assertThat(response.getProducts().get(2).getId()).isEqualTo("p3");

        verify(crossSellTools).searchProducts(eq("prirodny sampon"), anyList());
        verify(crossSellTools).searchProducts(eq("sampon"), anyList());
        verify(crossSellTools).searchProducts(eq("esencialny"), anyList());
    }

    @Test
    void findCrossSellProducts_shouldHandleHairColorRule() {
        // Rule 2: "hair color" (not henna) -> "biokap sampon", "biokap kondicioner", "prirodny sampon"
        mockSearch("biokap sampon", createProduct("p1", "Biokap Sampon"));
        mockSearch("biokap kondicioner", createProduct("p2", "Biokap Kondicioner"));
        mockSearch("prirodny sampon", createProduct("p3", "Prirodny Sampon"));

        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts("Hair Color Cream", Collections.emptyList());

        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.getProducts().get(0).getId()).isEqualTo("p1");
        assertThat(response.getProducts().get(1).getId()).isEqualTo("p2");
        assertThat(response.getProducts().get(2).getId()).isEqualTo("p3");
    }

    @Test
    void findCrossSellProducts_shouldHandleVonneTycinkyRule() {
        // Rule 3: "vonne tycinky" -> "stojan", "vonne tycinky"
        mockSearch("stojan", createProduct("p1", "Stojan"));
        mockSearch("vonne tycinky", createProduct("p2", "Vonne Tycinky Extra"));

        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts("Vonne tycinky sandal", Collections.emptyList());

        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts().get(0).getId()).isEqualTo("p1");
        assertThat(response.getProducts().get(1).getId()).isEqualTo("p2");
    }

    @Test
    void findCrossSellProducts_shouldHandleTuhySamponRule() {
        // Rule 4: "tuhy sampon" -> "mydelnicka", "mydlo"
        mockSearch("mydelnicka", createProduct("p1", "Mydelnicka"));
        mockSearch("mydlo", createProduct("p2", "Mydlo"));

        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts("Tuhy sampon bio", Collections.emptyList());

        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts().get(0).getId()).isEqualTo("p1");
        assertThat(response.getProducts().get(1).getId()).isEqualTo("p2");
    }

    @Test
    void findCrossSellProducts_shouldHandleDefaultRule() {
        // Rule 5: Default -> "sojova sviecka", "kefka", "vrecusko"
        mockSearch("sojova sviecka", createProduct("p1", "Sviecka"));
        mockSearch("kefka", createProduct("p2", "Kefka"));
        mockSearch("vrecusko", createProduct("p3", "Vrecusko"));

        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts("Random Product", Collections.emptyList());

        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.getProducts().get(0).getId()).isEqualTo("p1");
        assertThat(response.getProducts().get(1).getId()).isEqualTo("p2");
        assertThat(response.getProducts().get(2).getId()).isEqualTo("p3");
    }

    @Test
    void findCrossSellProducts_shouldRespectExclusionsAndDuplicates() {
        // Setup: First search returns p1, p2. We want max 3. Exclude p1 initially.
        // Result should be p2, and then whatever comes next.

        // Mock "sojova sviecka" returning p1 and p2. p1 is excluded by input.
        List<CrossSellProductDto> search1 = new ArrayList<>();
        search1.add(createProduct("p1", "Sviecka 1"));
        search1.add(createProduct("p2", "Sviecka 2"));
        when(crossSellTools.searchProducts(eq("sojova sviecka"), anyList())).thenReturn(search1);

        // Current list has p2 (size 1). Need 2 more.
        // Next search "kefka" returns p2 (duplicate) and p3.
        List<CrossSellProductDto> search2 = new ArrayList<>();
        search2.add(createProduct("p2", "Sviecka 2")); // Duplicate
        search2.add(createProduct("p3", "Kefka"));
        when(crossSellTools.searchProducts(eq("kefka"), anyList())).thenReturn(search2);

        // Current list has p2, p3 (size 2). Need 1 more.
        // Next search "vrecusko" returns p4.
        List<CrossSellProductDto> search3 = new ArrayList<>();
        search3.add(createProduct("p4", "Vrecusko"));
        when(crossSellTools.searchProducts(eq("vrecusko"), anyList())).thenReturn(search3);

        CrossSellProductsResponse response = crossSellAssistant.findCrossSellProducts("Random Product", List.of("p1"));

        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.getProducts().get(0).getId()).isEqualTo("p2");
        assertThat(response.getProducts().get(1).getId()).isEqualTo("p3");
        assertThat(response.getProducts().get(2).getId()).isEqualTo("p4");
    }

    private void mockSearch(String query, CrossSellProductDto... products) {
        when(crossSellTools.searchProducts(eq(query), anyList())).thenReturn(List.of(products));
    }

    private CrossSellProductDto createProduct(String id, String title) {
        CrossSellProductDto dto = new CrossSellProductDto();
        dto.setId(id);
        dto.setTitle(title);
        return dto;
    }
}
