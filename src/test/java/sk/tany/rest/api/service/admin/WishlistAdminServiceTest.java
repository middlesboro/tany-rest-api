package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminListResponse;
import sk.tany.rest.api.dto.admin.wishlist.WishlistCreateRequest;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.mapper.WishlistMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistAdminServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistMapper wishlistMapper;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistAdminServiceImpl wishlistAdminService;

    @Test
    void findAll_ShouldReturnPageOfWishlistAdminListResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Wishlist wishlist = new Wishlist("1", "cust1", "prod1", null);

        when(wishlistRepository.findAll()).thenReturn(Collections.singletonList(wishlist));

        Customer customer = new Customer();
        customer.setFirstname("John");
        customer.setLastname("Doe");
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Product product = new Product();
        product.setTitle("Product 1");
        when(productRepository.findById("prod1")).thenReturn(Optional.of(product));

        Page<WishlistAdminListResponse> result = wishlistAdminService.findAll(pageable);

        assertEquals(1, result.getContent().size());
        WishlistAdminListResponse response = result.getContent().getFirst();
        assertEquals("cust1", response.getCustomerId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(1, response.getProductNames().size());
        assertEquals("Product 1", response.getProductNames().getFirst());

        verify(wishlistRepository).findAll();
    }

    @Test
    void create_ShouldCreateWishlist_WhenNotExists() {
        WishlistCreateRequest request = new WishlistCreateRequest();
        request.setCustomerId("cust1");
        request.setProductId("prod1");
        when(wishlistRepository.findByCustomerIdAndProductId("cust1", "prod1")).thenReturn(Optional.empty());
        when(wishlistMapper.toAdminDto(any())).thenReturn(new WishlistAdminDto());

        wishlistAdminService.create(request);

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void create_ShouldThrowConflict_WhenExists() {
        WishlistCreateRequest request = new WishlistCreateRequest();
        request.setCustomerId("cust1");
        request.setProductId("prod1");
        when(wishlistRepository.findByCustomerIdAndProductId("cust1", "prod1")).thenReturn(Optional.of(new Wishlist()));

        assertThrows(ProductException.Conflict.class, () -> wishlistAdminService.create(request));
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void delete_ShouldDeleteWishlist() {
        String id = "1";
        wishlistAdminService.delete(id);
        verify(wishlistRepository).deleteById(id);
    }
}
