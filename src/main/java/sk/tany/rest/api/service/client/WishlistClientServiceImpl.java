package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.mapper.ProductMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistClientServiceImpl implements WishlistClientService {

    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SecurityUtil securityUtil;

    private String getCurrentCustomerId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new ProductException.BadRequest("User not logged in");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(email)
                .map(Customer::getId)
                .orElseThrow(() -> new ProductException.NotFound("Customer not found"));
    }

    @Override
    public void addToWishlist(String productId) {
        String customerId = getCurrentCustomerId();
        Optional<Wishlist> existing = wishlistRepository.findByCustomerIdAndProductId(customerId, productId);
        if (existing.isPresent()) {
            return; // Already in wishlist
        }
        Wishlist wishlist = new Wishlist();
        wishlist.setCustomerId(customerId);
        wishlist.setProductId(productId);
        wishlistRepository.save(wishlist);
    }

    @Override
    public void removeFromWishlist(String productId) {
        String customerId = getCurrentCustomerId();
        wishlistRepository.findByCustomerIdAndProductId(customerId, productId)
                .ifPresent(wishlistRepository::delete);
    }

    @Override
    public List<String> getWishlistProductIds() {
        return wishlistRepository.findByCustomerId(securityUtil.getLoggedInUserId()).stream()
                .map(Wishlist::getProductId)
                .toList();
    }

    @Override
    public Page<ProductClientDto> getWishlist(String customerId, Pageable pageable) {
        List<String> productIds = wishlistRepository.findByCustomerId(customerId).stream()
                .map(Wishlist::getProductId)
                .toList();

        List<ProductClientDto> products = productRepository.findAllById(productIds).stream()
                .map(product -> {
                    ProductClientDto dto = productMapper.toClientDto(product);
                    dto.setInWishlist(true);
                    return dto;
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());

        if (start > products.size()) {
            return new PageImpl<>(List.of(), pageable, products.size());
        }

        List<ProductClientDto> pageContent = products.subList(start, end);
        return new PageImpl<>(pageContent, pageable, products.size());
    }
}
