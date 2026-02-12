package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerClientServiceImplPaymentSortTest {

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
    void getCustomerContext_ShouldSortPaymentsByOrder() {
        // Arrange
        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        when(cartService.findCart("cart1")).thenReturn(java.util.Optional.of(cartDto));

        // Mock Carrier Service to return empty page
        when(carrierService.findAll(any(Pageable.class))).thenReturn(Page.empty());

        PaymentDto p1 = new PaymentDto();
        p1.setId("1");
        p1.setOrder(2);

        PaymentDto p2 = new PaymentDto();
        p2.setId("2");
        p2.setOrder(1);

        PaymentDto p3 = new PaymentDto();
        p3.setId("3");
        p3.setOrder(3);

        List<PaymentDto> payments = Arrays.asList(p1, p2, p3);
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(payments));

        // Act
        CustomerContextDto result = customerClientService.getCustomerContext("cart1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCartDto()).isNotNull();
        assertThat(result.getCartDto().getPayments()).isNotNull();
        assertThat(result.getCartDto().getPayments()).hasSize(3);

        // Check order: p2 (1), p1 (2), p3 (3)
        assertThat(result.getCartDto().getPayments().getFirst().getId()).isEqualTo("2");
        assertThat(result.getCartDto().getPayments().get(1).getId()).isEqualTo("1");
        assertThat(result.getCartDto().getPayments().get(2).getId()).isEqualTo("3");
    }

    @Test
    void getCustomerContext_ShouldSortPaymentsByOrder_WithNullsLast() {
        // Arrange
        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        when(cartService.findCart("cart1")).thenReturn(java.util.Optional.of(cartDto));

        // Mock Carrier Service to return empty page
        when(carrierService.findAll(any(Pageable.class))).thenReturn(Page.empty());

        PaymentDto p1 = new PaymentDto();
        p1.setId("1");
        p1.setOrder(null);

        PaymentDto p2 = new PaymentDto();
        p2.setId("2");
        p2.setOrder(1);

        List<PaymentDto> payments = Arrays.asList(p1, p2);
        when(paymentService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(payments));

        // Act
        CustomerContextDto result = customerClientService.getCustomerContext("cart1");

        // Assert
        // Check order: p2 (1), p1 (null)
        assertThat(result.getCartDto().getPayments().getFirst().getId()).isEqualTo("2");
        assertThat(result.getCartDto().getPayments().get(1).getId()).isEqualTo("1");
    }
}
