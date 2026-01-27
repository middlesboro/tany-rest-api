package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.mapper.CartDiscountMapper;
import sk.tany.rest.api.mapper.CartMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartClientServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private ProductClientService productService;
    @Mock
    private CartDiscountRepository cartDiscountRepository;
    @Mock
    private CartDiscountMapper cartDiscountMapper;
    // We don't need CarrierRepository and PaymentRepository for this test if we don't set carrier/payment

    @InjectMocks
    private CartClientServiceImpl service;

    @Test
    void addProductToCart_shouldApplyAutomaticDiscountAndSaveCode() {
        // Given
        String productId = "p1";
        ProductClientDto productDto = new ProductClientDto();
        productDto.setId(productId);
        productDto.setTitle("Test Product");
        productDto.setPrice(BigDecimal.valueOf(100));
        productDto.setQuantity(10);

        when(productService.findById(productId)).thenReturn(Optional.of(productDto));
        when(productService.findAllByIds(List.of(productId))).thenReturn(List.of(productDto));

        // Automatic discount definition
        CartDiscount autoDiscount = new CartDiscount();
        autoDiscount.setId("d1");
        autoDiscount.setCode("AUTO_CODE");
        autoDiscount.setAutomatic(true);
        autoDiscount.setActive(true);
        autoDiscount.setDiscountType(DiscountType.PERCENTAGE);
        autoDiscount.setValue(BigDecimal.valueOf(10)); // 10% off
        autoDiscount.setProductIds(List.of(productId)); // Applies to p1

        when(cartDiscountRepository.findAllByCodeIsNullAndActiveTrue()).thenReturn(Collections.emptyList());
        when(cartDiscountRepository.findAllByAutomaticTrueAndActiveTrue()).thenReturn(List.of(autoDiscount));

        // Mock mapper to return a client DTO for the discount (needed for cartDto.setAppliedDiscounts)
        CartDiscountClientDto discountClientDto = new CartDiscountClientDto();
        discountClientDto.setCode("AUTO_CODE");
        when(cartDiscountMapper.toClientDto(autoDiscount)).thenReturn(discountClientDto);

        // Mock save to return something to avoid NPE
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            c.setId("cart1");
            return c;
        });
        when(cartMapper.toDto(any(Cart.class))).thenReturn(new CartDto());

        // When
        service.addProductToCart(null, productId, 1);

        // Then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertNotNull(savedCart.getDiscountCodes());
        assertTrue(savedCart.getDiscountCodes().contains("AUTO_CODE"), "Cart should contain the automatic discount code");
    }
}
