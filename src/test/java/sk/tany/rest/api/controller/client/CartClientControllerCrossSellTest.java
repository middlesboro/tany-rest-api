package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.CrossSellProductDto;
import sk.tany.rest.api.dto.CrossSellProductsResponse;
import sk.tany.rest.api.dto.CrossSellResponse;
import sk.tany.rest.api.service.chat.CrossSellAssistant;
import sk.tany.rest.api.service.client.CartClientService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartClientControllerCrossSellTest {

    @Mock
    private CartClientService cartService;

    @Mock
    private CrossSellAssistant crossSellAssistant;

    @InjectMocks
    private CartClientController cartClientController;

    @Test
    void getCrossSell_shouldReturnRecommendations() {
        String cartId = "cart123";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);

        CartItem item = new CartItem();
        item.setProductId("p1");
        item.setTitle("Henna");
        cartDto.setItems(List.of(item));

        CrossSellProductDto recommendation = new CrossSellProductDto();
        recommendation.setId("p2");
        recommendation.setTitle("Shampoo");
        recommendation.setSlug("shampoo");
        recommendation.setPrice(BigDecimal.TEN);
        recommendation.setImage("img.jpg");

        when(cartService.getOrCreateCart(cartId, null)).thenReturn(cartDto);

        CrossSellProductsResponse crossSellProductsResponse = new CrossSellProductsResponse();
        crossSellProductsResponse.setProducts(List.of(recommendation));
        when(crossSellAssistant.findCrossSellProducts(eq("Henna"), anyList()))
                .thenReturn(crossSellProductsResponse);

        ResponseEntity<List<CrossSellResponse>> response = cartClientController.getCrossSell(cartId);

        assertThat(response.getBody()).hasSize(1);
        CrossSellResponse res = response.getBody().get(0);
        assertThat(res.getSourceProductId()).isEqualTo("p1");
        assertThat(res.getCrossSellProducts()).hasSize(1);
        assertThat(res.getCrossSellProducts().get(0).getTitle()).isEqualTo("Shampoo");
    }

    @Test
    void getCrossSell_shouldReturnEmptyIfCartEmpty() {
        String cartId = "cart123";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setItems(Collections.emptyList());

        when(cartService.getOrCreateCart(cartId, null)).thenReturn(cartDto);

        ResponseEntity<List<CrossSellResponse>> response = cartClientController.getCrossSell(cartId);

        assertThat(response.getBody()).isEmpty();
    }
}
