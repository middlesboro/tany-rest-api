package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.*;
import sk.tany.rest.api.mapper.CustomerMapper;
import sk.tany.rest.api.service.client.CustomerClientServiceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private CustomerClientServiceImpl customerClientService;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

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

        when(cartService.getOrCreateCart(cartId, null)).thenReturn(cartDto);

        ProductDto productDto = new ProductDto();
        productDto.setId("prod1");
        productDto.setPrice(BigDecimal.valueOf(10.0));
        productDto.setWeight(BigDecimal.valueOf(2.5)); // Total weight = 2 * 2.5 = 5.0
        when(productService.findAllByIds(List.of("prod1"))).thenReturn(List.of(productDto));

        CarrierDto carrier1 = new CarrierDto();
        carrier1.setId("carrier1");
        CarrierPriceRangeDto range1 = new CarrierPriceRangeDto();
        range1.setWeightFrom(BigDecimal.ZERO);
        range1.setWeightTo(BigDecimal.valueOf(10));
        range1.setPrice(BigDecimal.valueOf(5.0));
        carrier1.setRanges(List.of(range1));

        CarrierDto carrier2 = new CarrierDto();
        carrier2.setId("carrier2");
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
}
