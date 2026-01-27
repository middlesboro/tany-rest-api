package sk.tany.rest.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.admin.cart.get.CartAdminGetResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartAdminApiMapperTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;

    // Since CartAdminApiMapper is abstract and we are testing the logic implemented in the abstract class methods
    // we need to instantiate a concrete subclass or use Mockito to mock abstract class call real method.
    // However, MapStruct generates the implementation.
    // In a unit test environment without Spring/MapStruct generation, we can create an anonymous subclass.

    private CartAdminApiMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new CartAdminApiMapper() {
            @Override
            public CartDto toDto(sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateRequest request) {
                return null;
            }

            @Override
            public sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateResponse toCreateResponse(CartDto dto) {
                return null;
            }

            @Override
            protected CartAdminGetResponse mapBase(CartDto dto) {
                CartAdminGetResponse response = new CartAdminGetResponse();
                response.setCartId(dto.getCartId());
                return response;
            }

            @Override
            public CartDto toDto(sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateRequest request) {
                return null;
            }

            @Override
            public sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateResponse toUpdateResponse(CartDto dto) {
                return null;
            }
        };
        // Manually inject mocks into protected fields
        mapper.orderRepository = orderRepository;
        mapper.customerRepository = customerRepository;
        mapper.carrierRepository = carrierRepository;
        mapper.paymentRepository = paymentRepository;
    }

    @Test
    public void testToGetResponse_withOrder() {
        CartDto dto = new CartDto();
        dto.setCartId("cart1");
        dto.setCustomerId("cust1");

        Order order = new Order();
        order.setOrderIdentifier(123L);
        order.setFinalPrice(BigDecimal.valueOf(100.00));
        order.setPriceBreakDown(new PriceBreakDown());
        order.setCarrierId("carr1");
        order.setPaymentId("pay1");

        Carrier carrier = new Carrier();
        carrier.setName("CarrierName");

        Payment payment = new Payment();
        payment.setName("PaymentName");

        Customer customer = new Customer();
        customer.setFirstname("John");
        customer.setLastname("Doe");

        when(orderRepository.findByCartId("cart1")).thenReturn(Optional.of(order));
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));
        when(carrierRepository.findById("carr1")).thenReturn(Optional.of(carrier));
        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));

        CartAdminGetResponse response = mapper.toGetResponse(dto);

        assertEquals("cart1", response.getCartId());
        assertEquals(123L, response.getOrderIdentifier());
        assertEquals(BigDecimal.valueOf(100.00), response.getPrice());
        assertNotNull(response.getPriceBreakDown());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("CarrierName", response.getCarrierName());
        assertEquals("PaymentName", response.getPaymentName());
    }

    @Test
    public void testToGetResponse_noOrder_calculatePrice() {
        CartDto dto = new CartDto();
        dto.setCartId("cart2");
        dto.setItems(List.of(
            new CartItem("p1", 2, "Title", "img", BigDecimal.valueOf(10.0)),
            new CartItem("p2", 1, "Title2", "img", BigDecimal.valueOf(20.0))
        ));

        when(orderRepository.findByCartId("cart2")).thenReturn(Optional.empty());

        CartAdminGetResponse response = mapper.toGetResponse(dto);

        // 2*10 + 1*20 = 40
        assertEquals(BigDecimal.valueOf(40.0), response.getPrice());
        assertEquals(null, response.getOrderIdentifier());
    }
}
