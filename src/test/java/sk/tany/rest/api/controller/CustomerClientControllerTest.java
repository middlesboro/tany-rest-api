package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sk.tany.rest.api.controller.client.CustomerClientController;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;
import sk.tany.rest.api.mapper.CustomerClientApiMapper;
import sk.tany.rest.api.service.client.CustomerClientService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerClientService customerService;

    @Mock
    private CustomerClientApiMapper customerClientApiMapper;

    @InjectMocks
    private CustomerClientController customerClientController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerClientController).build();
    }

    @Test
    void getCustomerContext_ReturnsOk() throws Exception {
        String cartId = "test-cart-id";
        CustomerContextDto customerContextDto = new CustomerContextDto();
        customerContextDto.setCustomerDto(new CustomerDto());
        customerContextDto.setCartDto(new CustomerContextCartDto());

        CustomerClientGetResponse response = new CustomerClientGetResponse();
        response.setCustomerDto(new CustomerDto());
        response.setCartDto(new CustomerContextCartDto());

        when(customerService.getCustomerContext(cartId)).thenReturn(customerContextDto);
        when(customerClientApiMapper.toGetResponse(customerContextDto)).thenReturn(response);

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

        CustomerClientGetResponse response = new CustomerClientGetResponse();
        response.setCustomerDto(new CustomerDto());
        response.setCartDto(new CustomerContextCartDto());

        when(customerService.getCustomerContext(null)).thenReturn(customerContextDto);
        when(customerClientApiMapper.toGetResponse(customerContextDto)).thenReturn(response);

        mockMvc.perform(get("/api/customer/context"))
                .andExpect(status().isOk());

        verify(customerService).getCustomerContext(null);
    }
}
