package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "prestashop.url=http://mock-prestashop.com",
        "prestashop.key=mock-key",
        "eshop.frontend-url=http://127.0.0.1:3001",
        "eshop.base-url=http://localhost:8080"
})
@AutoConfigureMockMvc
public class AuthorizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private EmailService emailService;

    @Test
    public void testRequestMagicLinkUnknownEmailReturns200() throws Exception {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/magic-link/request")
                .param("email", "unknown@example.com"))
                .andExpect(status().isOk());

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), eq(true), any());
    }

    @Test
    public void testRateLimiting() throws Exception {
        when(customerRepository.findByEmail("rate@example.com")).thenReturn(Optional.of(new Customer()));

        // First request - OK
        mockMvc.perform(post("/auth/magic-link/request")
                .param("email", "rate@example.com"))
                .andExpect(status().isOk());

        // Second request immediately - Too Many Requests
        mockMvc.perform(post("/auth/magic-link/request")
                .param("email", "rate@example.com"))
                .andExpect(status().isTooManyRequests());
    }
}
