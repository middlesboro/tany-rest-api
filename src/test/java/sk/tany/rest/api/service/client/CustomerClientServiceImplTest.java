package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CarrierPriceRangeDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.mapper.CustomerMapper;
import sk.tany.rest.api.component.SecurityUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerClientServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CartClientService cartService;

    @Mock
    private ProductClientService productService;

    @Mock
    private CarrierClientService carrierService;

    @Mock
    private PaymentClientService paymentService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private CustomerClientServiceImpl customerClientService;

    @Test
    void getCustomerContext_CalculatesTotalAndSelectsOptions() {
        // Arrange
        String cartId = "cart1";
        String customerId = "cust1";
        String selectedCarrierId = "carrier1";
        String selectedPaymentId = "payment1";

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setCustomerId(customerId);
        cartDto.setSelectedCarrierId(selectedCarrierId);
        cartDto.setSelectedPaymentId(selectedPaymentId);
        CartItem cartItem = new CartItem("prod1", 2);
        cartDto.setItems(List.of(cartItem));

        when(cartService.findCart(cartId)).thenReturn(Optional.of(cartDto));

        ProductClientDto productDto = new ProductClientDto();
        productDto.setId("prod1");
        productDto.setPrice(BigDecimal.valueOf(10.0));
        productDto.setWeight(BigDecimal.valueOf(2.5)); // Total weight = 2 * 2.5 = 5.0
        when(productService.findAllByIds(List.of("prod1"))).thenReturn(List.of(productDto));

        CarrierDto carrier1 = new CarrierDto();
        carrier1.setId("carrier1");
        carrier1.setOrder(1);
        CarrierPriceRangeDto range1 = new CarrierPriceRangeDto();
        range1.setWeightFrom(BigDecimal.ZERO);
        range1.setWeightTo(BigDecimal.valueOf(10));
        range1.setPrice(BigDecimal.valueOf(5.0));
        carrier1.setRanges(List.of(range1));

        CarrierDto carrier2 = new CarrierDto();
        carrier2.setId("carrier2");
        carrier2.setOrder(2);
        CarrierPriceRangeDto range2 = new CarrierPriceRangeDto();
        range2.setWeightFrom(BigDecimal.valueOf(10.1));
        range2.setWeightTo(BigDecimal.valueOf(20));
        range2.setPrice(BigDecimal.valueOf(10.0));
        carrier2.setRanges(List.of(range2)); // Should not fit

        when(carrierService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(carrier1, carrier2)));

        PaymentDto payment1 = new PaymentDto();
        payment1.setId("payment1");
        PaymentDto payment2 = new PaymentDto();
        payment2.setId("payment2");
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(payment1, payment2)));

        // Act
        CustomerContextDto result = customerClientService.getCustomerContext(cartId);

        // Assert
        CustomerContextCartDto contextCart = result.getCartDto();

        // Verify Total Price: 2 * 10.0 = 20.0
        assertEquals(0, BigDecimal.valueOf(20.0).compareTo(contextCart.getTotalProductPrice()));

        // Verify Carriers
        assertEquals(2, contextCart.getCarriers().size());
        assertTrue(contextCart.getCarriers().stream().anyMatch(c -> c.getId().equals("carrier1") && c.isSelected() && BigDecimal.valueOf(5.0).compareTo(c.getPrice()) == 0));
        assertTrue(contextCart.getCarriers().stream().anyMatch(c -> c.getId().equals("carrier2") && !c.isSelected() && c.getPrice() == null));

        // Verify ranges are cleared
        assertTrue(contextCart.getCarriers().stream().allMatch(c -> c.getRanges() == null));

        // Verify Payments
        assertEquals(2, contextCart.getPayments().size());
        assertTrue(contextCart.getPayments().stream().anyMatch(p -> p.getId().equals("payment1") && p.isSelected()));
        assertTrue(contextCart.getPayments().stream().anyMatch(p -> p.getId().equals("payment2") && !p.isSelected()));
    }

    @Test
    void getCustomerContext_SelectsFirstOption_WhenNoneSelected() {
        // Arrange
        String cartId = "cart1";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        // selectedCarrierId and selectedPaymentId are null

        when(cartService.findCart(cartId)).thenReturn(Optional.of(cartDto));

        CarrierDto carrier1 = new CarrierDto();
        carrier1.setId("carrier1");
        carrier1.setOrder(1);
        CarrierDto carrier2 = new CarrierDto();
        carrier2.setId("carrier2");
        carrier2.setOrder(2);
        when(carrierService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(carrier1, carrier2)));

        PaymentDto payment1 = new PaymentDto();
        payment1.setId("payment1");
        PaymentDto payment2 = new PaymentDto();
        payment2.setId("payment2");
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(payment1, payment2)));

        // Act
        CustomerContextDto result = customerClientService.getCustomerContext(cartId);
        CustomerContextCartDto contextCart = result.getCartDto();

        // Assert
        // First carrier should be selected
        assertTrue(contextCart.getCarriers().getFirst().isSelected());
        assertFalse(contextCart.getCarriers().get(1).isSelected());

        // First payment should be selected
        assertTrue(contextCart.getPayments().getFirst().isSelected());
        assertFalse(contextCart.getPayments().get(1).isSelected());
    }

    @Test
    void getCustomerContext_SetsDiscountForNewsletter_WhenDiscountApplied() {
        // Arrange
        String cartId = "cart1";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);

        sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto discount = new sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto();
        discount.setCode("zlava10");
        cartDto.setAppliedDiscounts(List.of(discount));
        cartDto.setDiscountForNewsletter(true);

        when(cartService.findCart(cartId)).thenReturn(Optional.of(cartDto));
        when(carrierService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        CustomerContextDto result = customerClientService.getCustomerContext(cartId);

        // Assert
        assertTrue(result.isDiscountForNewsletter());
    }

    @Test
    void getCustomerContext_SetsDiscountForNewsletter_WhenDiscountNotApplied() {
        // Arrange
        String cartId = "cart1";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);

        sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto discount = new sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto();
        discount.setCode("other");
        cartDto.setAppliedDiscounts(List.of(discount));

        when(cartService.findCart(cartId)).thenReturn(Optional.of(cartDto));
        when(carrierService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        CustomerContextDto result = customerClientService.getCustomerContext(cartId);

        // Assert
        assertFalse(result.isDiscountForNewsletter());
    }
}
