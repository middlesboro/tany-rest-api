package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;
import sk.tany.rest.api.dto.admin.wishlist.WishlistCreateRequest;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.mapper.WishlistMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistAdminServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistAdminServiceImpl wishlistAdminService;

    @Test
    void findAll_ShouldReturnPageOfWishlistAdminDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Wishlist wishlist = new Wishlist("1", "cust1", "prod1", null);
        Page<Wishlist> wishlistPage = new org.springframework.data.domain.PageImpl<>(Collections.singletonList(wishlist));
        when(wishlistRepository.findAll(pageable)).thenReturn(wishlistPage);
        when(wishlistMapper.toAdminDto(wishlist)).thenReturn(new WishlistAdminDto());

        Page<WishlistAdminDto> result = wishlistAdminService.findAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(wishlistRepository).findAll(pageable);
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
