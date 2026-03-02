package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ShopSettingsRepository shopSettingsRepository;

    @MockitoBean
    private org.springframework.security.web.context.SecurityContextRepository securityContextRepository;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private sk.tany.rest.api.config.EshopConfig eshopConfig;

    @MockitoBean
    private sk.tany.rest.api.config.CorsConfig corsConfig;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.springframework.security.core.context.SecurityContext emptyContext = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        org.springframework.security.core.context.DeferredSecurityContext deferredContext = new org.springframework.security.core.context.DeferredSecurityContext() {
            @Override
            public org.springframework.security.core.context.SecurityContext get() {
                return emptyContext;
            }

            @Override
            public boolean isGenerated() {
                return false;
            }
        };
        when(securityContextRepository.loadDeferredContext(any(jakarta.servlet.http.HttpServletRequest.class)))
                .thenReturn(deferredContext);
    }

    @Test
    public void testRequestMagicLinkUnknownEmailReturns200() throws Exception {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/magic-link/request")
                .with(csrf())
                .param("email", "unknown@example.com"))
                .andExpect(status().isOk());

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), eq(true), any());
    }

    @Test
    public void testRateLimiting() throws Exception {
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("test@test.com");
        settings.setShopPhoneNumber("123456789");
        when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);

        when(customerRepository.findByEmail("rate@example.com")).thenReturn(Optional.of(new Customer()));

        // First request - OK
        mockMvc.perform(post("/auth/magic-link/request")
                .with(csrf())
                .param("email", "rate@example.com"))
                .andExpect(status().isOk());

        // Second request immediately - Too Many Requests
        mockMvc.perform(post("/auth/magic-link/request")
                .with(csrf())
                .param("email", "rate@example.com"))
                .andExpect(status().isTooManyRequests());
    }
}
