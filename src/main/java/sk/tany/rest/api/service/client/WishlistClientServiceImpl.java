package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.exception.ProductException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistClientServiceImpl implements WishlistClientService {

    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;
    private final ProductClientService productClientService;

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
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
            "anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return Collections.emptyList();
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isEmpty()) {
            return Collections.emptyList();
        }

        return wishlistRepository.findByCustomerId(customer.get().getId()).stream()
                .map(Wishlist::getProductId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductClientDto> getWishlist(String customerId, Pageable pageable) {
        List<String> productIds = wishlistRepository.findByCustomerId(customerId).stream()
                .map(Wishlist::getProductId)
                .collect(Collectors.toList());

        List<ProductClientDto> products = productClientService.findAllByIds(productIds);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());

        if (start > products.size()) {
            return new PageImpl<>(List.of(), pageable, products.size());
        }

        List<ProductClientDto> pageContent = products.subList(start, end);
        return new PageImpl<>(pageContent, pageable, products.size());
    }
}
