package sk.tany.rest.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateRequest;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateResponse;
import sk.tany.rest.api.dto.admin.order.get.OrderAdminGetResponse;
import sk.tany.rest.api.dto.admin.order.list.OrderAdminListResponse;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderAdminApiMapperTest {

    @Mock
    private CustomerRepository customerRepository;

    private OrderAdminApiMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new OrderAdminApiMapper() {
            @Override
            public OrderDto toDto(OrderAdminCreateRequest request) { return null; }
            @Override
            public OrderAdminCreateResponse toCreateResponse(OrderDto dto) { return null; }
            @Override
            public OrderAdminGetResponse toGetResponse(OrderDto dto) {
                // Simulate MapStruct generated code which calls the annotated method
                OrderAdminGetResponse response = new OrderAdminGetResponse();
                response.setCustomerName(resolveCustomerName(dto));
                response.setEmail(dto.getEmail());
                response.setPhone(dto.getPhone());
                return response;
            }
            @Override
            public OrderAdminListResponse toListResponse(OrderDto dto) { return null; }
            @Override
            public OrderDto toDto(OrderAdminUpdateRequest request) { return null; }
            @Override
            public OrderAdminUpdateResponse toUpdateResponse(OrderDto dto) { return null; }
        };
        mapper.customerRepository = customerRepository;
    }

    @Test
    public void testResolveCustomerName_fromRepository() {
        OrderDto dto = new OrderDto();
        dto.setCustomerId("cust1");
        dto.setFirstname("OrderJohn");
        dto.setLastname("OrderDoe");

        Customer customer = new Customer();
        customer.setFirstname("DbJohn");
        customer.setLastname("DbDoe");

        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        OrderAdminGetResponse response = mapper.toGetResponse(dto);
        assertEquals("DbJohn DbDoe", response.getCustomerName());
    }

    @Test
    public void testResolveCustomerName_fallbackToDto() {
        OrderDto dto = new OrderDto();
        dto.setCustomerId("cust2"); // Not found in DB
        dto.setFirstname("OrderJohn");
        dto.setLastname("OrderDoe");

        when(customerRepository.findById("cust2")).thenReturn(Optional.empty());

        OrderAdminGetResponse response = mapper.toGetResponse(dto);
        assertEquals("OrderJohn OrderDoe", response.getCustomerName());
    }

    @Test
    public void testResolveCustomerName_fallbackToDto_NullCustomerId() {
        OrderDto dto = new OrderDto();
        dto.setCustomerId(null);
        dto.setFirstname("OrderJohn");
        dto.setLastname("OrderDoe");

        OrderAdminGetResponse response = mapper.toGetResponse(dto);
        assertEquals("OrderJohn OrderDoe", response.getCustomerName());
    }

    @Test
    public void testEmailAndPhoneMapping() {
        OrderDto dto = new OrderDto();
        dto.setEmail("test@example.com");
        dto.setPhone("123456789");

        OrderAdminGetResponse response = mapper.toGetResponse(dto);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("123456789", response.getPhone());
    }
}
