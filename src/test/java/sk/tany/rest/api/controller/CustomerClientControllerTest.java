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
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientDetailResponse;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateRequest;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateResponse;
import sk.tany.rest.api.mapper.CustomerClientApiMapper;
import sk.tany.rest.api.service.client.CustomerClientService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        response.setCustomerDto(new CustomerClientGetResponse.CustomerDto());
        response.setCartDto(new CustomerClientGetResponse.CustomerContextCartDto());

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
        response.setCustomerDto(new CustomerClientGetResponse.CustomerDto());
        response.setCartDto(new CustomerClientGetResponse.CustomerContextCartDto());

        when(customerService.getCustomerContext(null)).thenReturn(customerContextDto);
        when(customerClientApiMapper.toGetResponse(customerContextDto)).thenReturn(response);

        mockMvc.perform(get("/api/customer/context"))
                .andExpect(status().isOk());

        verify(customerService).getCustomerContext(null);
    }

    @Test
    void getCustomer_ReturnsOk() throws Exception {
        CustomerDto customerDto = new CustomerDto();
        CustomerClientDetailResponse response = new CustomerClientDetailResponse();

        when(customerService.getCurrentCustomer()).thenReturn(customerDto);
        when(customerClientApiMapper.toDetailResponse(customerDto)).thenReturn(response);

        mockMvc.perform(get("/api/customer"))
                .andExpect(status().isOk());

        verify(customerService).getCurrentCustomer();
    }

    @Test
    void updateCustomer_ReturnsOk() throws Exception {
        CustomerClientUpdateRequest request = new CustomerClientUpdateRequest();
        CustomerDto customerDto = new CustomerDto();
        CustomerDto updatedCustomer = new CustomerDto();
        CustomerClientUpdateResponse response = new CustomerClientUpdateResponse();

        when(customerClientApiMapper.toDto(request)).thenReturn(customerDto);
        when(customerService.getCurrentCustomer()).thenReturn(customerDto);
        when(customerService.updateCustomer(customerDto)).thenReturn(updatedCustomer);
        when(customerClientApiMapper.toUpdateResponse(updatedCustomer)).thenReturn(response);

        mockMvc.perform(put("/api/customer")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(customerService).updateCustomer(customerDto);
    }

    @Test
    void updateCustomer_WithDifferentId_ReturnsForbidden() throws Exception {
        CustomerClientUpdateRequest request = new CustomerClientUpdateRequest();
        request.setId("id1");

        CustomerDto requestCustomerDto = new CustomerDto();
        requestCustomerDto.setId("id1");

        CustomerDto currentCustomerDto = new CustomerDto();
        currentCustomerDto.setId("id2");

        when(customerClientApiMapper.toDto(request)).thenReturn(requestCustomerDto);
        when(customerService.getCurrentCustomer()).thenReturn(currentCustomerDto);

        mockMvc.perform(put("/api/customer")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
