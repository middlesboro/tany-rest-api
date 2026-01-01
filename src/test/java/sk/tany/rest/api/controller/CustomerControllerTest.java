package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.service.CustomerService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    void getCustomerContext_ReturnsOk() throws Exception {
        String cartId = "test-cart-id";
        CustomerContextDto customerContextDto = new CustomerContextDto();
        customerContextDto.setCustomerDto(new CustomerDto());
        customerContextDto.setCartDto(new CustomerContextCartDto());

        when(customerService.getCustomerContext(cartId)).thenReturn(customerContextDto);

        mockMvc.perform(get("/api/customer/context")
                        .param("cartId", cartId))
                .andExpect(status().isOk());

        verify(customerService).getCustomerContext(cartId);
    }

    @Test
    void getCustomerContext_WithNullCartId_ReturnsOk() throws Exception {
        CustomerContextDto customerContextDto = new CustomerContextDto();
        customerContextDto.setCustomerDto(new CustomerDto());
        customerContextDto.setCartDto(new CustomerContextCartDto());

        when(customerService.getCustomerContext(null)).thenReturn(customerContextDto);

        mockMvc.perform(get("/api/customer/context"))
                .andExpect(status().isOk());

        verify(customerService).getCustomerContext(null);
    }
}
