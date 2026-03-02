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
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;
import sk.tany.rest.api.domain.payment.PaymentRepository;
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
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private sk.tany.rest.api.component.ProductSearchEngine productSearchEngine;
    @Mock
    private sk.tany.rest.api.mapper.ProductClientApiMapper productClientApiMapper;

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

        when(cartDiscountRepository.findApplicableAutomaticDiscounts(any(), any(), any())).thenReturn(List.of(autoDiscount));

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

        // Mock productSearchEngine
        when(productSearchEngine.searchAndSort(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        // When
        service.addProductToCart(null, productId, 1);

        // Then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertNotNull(savedCart.getDiscountCodes());
        assertTrue(savedCart.getDiscountCodes().contains("AUTO_CODE"), "Cart should contain the automatic discount code");
    }

    @Test
    void addProductToCart_shouldNotApplyAutomaticDiscountIfProductRestrictionDoesNotMatch() {
        // Given
        String productId = "p1";
        ProductClientDto productDto = new ProductClientDto();
        productDto.setId(productId);
        productDto.setTitle("Test Product");
        productDto.setPrice(BigDecimal.valueOf(100));
        productDto.setQuantity(10);

        when(productService.findById(productId)).thenReturn(Optional.of(productDto));
        when(productService.findAllByIds(List.of(productId))).thenReturn(List.of(productDto));

        // Automatic discount definition for a DIFFERENT product
        CartDiscount autoDiscount = new CartDiscount();
        autoDiscount.setId("d1");
        autoDiscount.setCode("AUTO_CODE_OTHER");
        autoDiscount.setAutomatic(true);
        autoDiscount.setActive(true);
        autoDiscount.setDiscountType(DiscountType.PERCENTAGE);
        autoDiscount.setValue(BigDecimal.valueOf(10));
        autoDiscount.setProductIds(List.of("different_product_id")); // Restriction does NOT match p1

        when(cartDiscountRepository.findApplicableAutomaticDiscounts(any(), any(), any())).thenReturn(Collections.emptyList());

        // Mock save
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            c.setId("cart1");
            return c;
        });
        when(cartMapper.toDto(any(Cart.class))).thenReturn(new CartDto());

        // Mock productSearchEngine
        when(productSearchEngine.searchAndSort(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        // When
        service.addProductToCart(null, productId, 1);

        // Then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        // The automatic discount should NOT be in the list because it wasn't applicable
        if (savedCart.getDiscountCodes() != null) {
            assertFalse(savedCart.getDiscountCodes().contains("AUTO_CODE_OTHER"), "Cart should NOT contain the automatic discount code if restriction doesn't match");
        }
    }

    @Test
    void addCarrier_shouldApplyFreeShippingThreshold() {
        // Given
        String cartId = "cart1";
        String carrierId = "carrier1";
        String productId = "p1";

        Cart cart = new Cart();
        cart.setId(cartId);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setItems(new ArrayList<>());
        sk.tany.rest.api.dto.CartItem item = new sk.tany.rest.api.dto.CartItem(productId, 1);
        item.setPrice(BigDecimal.valueOf(100));
        cartDto.getItems().add(item);

        // Mock mapper to return DTO from entity
        // Called in findById (addCarrier) and findById (save)
        when(cartMapper.toDto(any(Cart.class))).thenReturn(cartDto);

        // Product Setup
        ProductClientDto product = new ProductClientDto();
        product.setId(productId);
        product.setTitle("Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setWeight(BigDecimal.ONE);
        when(productService.findAllByIds(anyList())).thenReturn(List.of(product));

        // Carrier Setup
        sk.tany.rest.api.domain.carrier.Carrier carrier = new sk.tany.rest.api.domain.carrier.Carrier();
        carrier.setId(carrierId);
        carrier.setName("Test Carrier");
        sk.tany.rest.api.domain.carrier.CarrierPriceRange range = new sk.tany.rest.api.domain.carrier.CarrierPriceRange();
        range.setWeightFrom(BigDecimal.ZERO);
        range.setWeightTo(BigDecimal.TEN);
        range.setPrice(BigDecimal.valueOf(10)); // Base shipping price
        range.setFreeShippingThreshold(BigDecimal.valueOf(50)); // Threshold < 100
        carrier.setRanges(List.of(range));

        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        CartDto result = service.addCarrier(cartId, carrierId);

        // Then
        assertNotNull(result.getPriceBreakDown());
        // Check Carrier Price Item
        Optional<sk.tany.rest.api.dto.PriceItem> carrierItem = result.getPriceBreakDown().getItems().stream()
                .filter(i -> i.getType() == sk.tany.rest.api.dto.PriceItemType.CARRIER)
                .findFirst();

        assertTrue(carrierItem.isPresent());
        assertEquals(0, BigDecimal.ZERO.compareTo(carrierItem.get().getPriceWithVat()), "Delivery price should be 0 because threshold is met");
    }

    @Test
    void addCarrier_shouldNotApplyFreeShippingThreshold_whenTotalIsLow() {
        // Given
        String cartId = "cart1";
        String carrierId = "carrier1";
        String productId = "p1";

        Cart cart = new Cart();
        cart.setId(cartId);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setItems(new ArrayList<>());
        sk.tany.rest.api.dto.CartItem item = new sk.tany.rest.api.dto.CartItem(productId, 1);
        item.setPrice(BigDecimal.valueOf(40)); // Total 40
        cartDto.getItems().add(item);

        when(cartMapper.toDto(any(Cart.class))).thenReturn(cartDto);

        ProductClientDto product = new ProductClientDto();
        product.setId(productId);
        product.setTitle("Product");
        product.setPrice(BigDecimal.valueOf(40));
        product.setWeight(BigDecimal.ONE);
        when(productService.findAllByIds(anyList())).thenReturn(List.of(product));

        sk.tany.rest.api.domain.carrier.Carrier carrier = new sk.tany.rest.api.domain.carrier.Carrier();
        carrier.setId(carrierId);
        carrier.setName("Test Carrier");
        sk.tany.rest.api.domain.carrier.CarrierPriceRange range = new sk.tany.rest.api.domain.carrier.CarrierPriceRange();
        range.setWeightFrom(BigDecimal.ZERO);
        range.setWeightTo(BigDecimal.TEN);
        range.setPrice(BigDecimal.valueOf(10));
        range.setPriceWithoutVat(BigDecimal.valueOf(8));
        range.setVatValue(BigDecimal.valueOf(2));
        range.setFreeShippingThreshold(BigDecimal.valueOf(50)); // Threshold > 40
        carrier.setRanges(List.of(range));

        when(carrierRepository.findById(carrierId)).thenReturn(Optional.of(carrier));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        CartDto result = service.addCarrier(cartId, carrierId);

        // Then
        Optional<sk.tany.rest.api.dto.PriceItem> carrierItem = result.getPriceBreakDown().getItems().stream()
                .filter(i -> i.getType() == sk.tany.rest.api.dto.PriceItemType.CARRIER)
                .findFirst();

        assertTrue(carrierItem.isPresent());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(carrierItem.get().getPriceWithVat()), "Delivery price should be standard because threshold is not met");
    }
}
