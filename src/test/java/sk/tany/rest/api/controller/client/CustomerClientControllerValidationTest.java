package sk.tany.rest.api.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateRequest;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateResponse;
import sk.tany.rest.api.mapper.CustomerClientApiMapper;
import sk.tany.rest.api.service.client.CustomerClientService;
import sk.tany.rest.api.service.client.emailnotification.EmailNotificationClientService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerClientController.class)
class CustomerClientControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerClientService customerService;

    @MockitoBean
    private CustomerClientApiMapper customerClientApiMapper;

    @MockitoBean
    private EmailNotificationClientService emailNotificationClientService;

    @MockitoBean
    private SecurityUtil securityUtil;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private JwkKeyRepository jwkKeyRepository;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @Test
    @WithMockUser
    void updateCustomer_WhenAddressIsInvalid_ShouldReturnBadRequest() throws Exception {
        CustomerClientUpdateRequest request = new CustomerClientUpdateRequest();
        AddressDto address = new AddressDto();
        address.setStreet(""); // Invalid
        address.setCity("City");
        address.setZip("12345");
        address.setCountry("Country");
        request.setInvoiceAddress(address);

        mockMvc.perform(put("/api/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateCustomer_WhenValid_ShouldReturnOk() throws Exception {
        CustomerClientUpdateRequest request = new CustomerClientUpdateRequest();
        AddressDto address = new AddressDto();
        address.setStreet("Street");
        address.setCity("City");
        address.setZip("12345");
        address.setCountry("Country");
        request.setInvoiceAddress(address);

        CustomerDto customerDto = new CustomerDto();
        when(customerClientApiMapper.toDto(any(CustomerClientUpdateRequest.class))).thenReturn(customerDto);
        when(securityUtil.getLoggedInUserId()).thenReturn("user-1");
        when(customerService.updateCustomer(any(CustomerDto.class))).thenReturn(customerDto);
        when(customerClientApiMapper.toUpdateResponse(any(CustomerDto.class))).thenReturn(new CustomerClientUpdateResponse());

        mockMvc.perform(put("/api/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
