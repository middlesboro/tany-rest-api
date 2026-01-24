package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.dto.client.product.ProductClientDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistClientServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductClientService productClientService;

    @InjectMocks
    private WishlistClientServiceImpl wishlistClientService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockUser(String email, String id) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        Customer customer = new Customer();
        customer.setId(id);
        customer.setEmail(email);
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
    }

    @Test
    void addToWishlist_ShouldAdd_WhenNotExists() {
        mockUser("user@example.com", "cust1");
        when(wishlistRepository.findByCustomerIdAndProductId("cust1", "prod1")).thenReturn(Optional.empty());

        wishlistClientService.addToWishlist("prod1");

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_ShouldNotAdd_WhenExists() {
        mockUser("user@example.com", "cust1");
        when(wishlistRepository.findByCustomerIdAndProductId("cust1", "prod1")).thenReturn(Optional.of(new Wishlist()));

        wishlistClientService.addToWishlist("prod1");

        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void removeFromWishlist_ShouldRemove_WhenExists() {
        mockUser("user@example.com", "cust1");
        Wishlist wishlist = new Wishlist();
        when(wishlistRepository.findByCustomerIdAndProductId("cust1", "prod1")).thenReturn(Optional.of(wishlist));

        wishlistClientService.removeFromWishlist("prod1");

        verify(wishlistRepository).delete(wishlist);
    }

    @Test
    void getWishlistProductIds_ShouldReturnIds() {
        mockUser("user@example.com", "cust1");
        when(securityContext.getAuthentication().getPrincipal()).thenReturn("user@example.com");
        Wishlist wishlist = new Wishlist("1", "cust1", "prod1", null);
        when(wishlistRepository.findByCustomerId("cust1")).thenReturn(Collections.singletonList(wishlist));

        List<String> result = wishlistClientService.getWishlistProductIds();

        assertEquals(1, result.size());
        assertEquals("prod1", result.get(0));
    }

    @Test
    void getWishlistProductIds_ShouldReturnEmpty_WhenNotLoggedIn() {
         when(securityContext.getAuthentication()).thenReturn(null);

         List<String> result = wishlistClientService.getWishlistProductIds();

         assertTrue(result.isEmpty());
    }

    @Test
    void getWishlist_ShouldReturnPageOfProducts() {
        String customerId = "cust1";
        Pageable pageable = PageRequest.of(0, 10);
        Wishlist wishlist = new Wishlist("1", customerId, "prod1", null);

        when(wishlistRepository.findByCustomerId(customerId)).thenReturn(Collections.singletonList(wishlist));

        ProductClientDto productDto = new ProductClientDto();
        productDto.setId("prod1");
        when(productClientService.findAllByIds(List.of("prod1"))).thenReturn(List.of(productDto));

        Page<ProductClientDto> result = wishlistClientService.getWishlist(customerId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("prod1", result.getContent().get(0).getId());
    }
}
