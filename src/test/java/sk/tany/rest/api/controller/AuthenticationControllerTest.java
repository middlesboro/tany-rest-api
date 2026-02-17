package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.MockRepositoriesConfig;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "prestashop.url=http://mock-prestashop.com",
        "prestashop.key=mock-key",
        "eshop.frontend-url=http://127.0.0.1:3001",
        "eshop.base-url=http://localhost:8080"
})
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class
})
@AutoConfigureMockMvc
@Import(MockRepositoriesConfig.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Repositories are mocked in MockRepositoriesConfig, so we Autowire the mock here
    @Autowired
    private CustomerRepository customerRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private ShopSettingsRepository shopSettingsRepository;

    @MockBean
    private org.springframework.security.web.context.SecurityContextRepository securityContextRepository;

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
                .param("email", "rate@example.com"))
                .andExpect(status().isOk());

        // Second request immediately - Too Many Requests
        mockMvc.perform(post("/auth/magic-link/request")
                .param("email", "rate@example.com"))
                .andExpect(status().isTooManyRequests());
    }
}
