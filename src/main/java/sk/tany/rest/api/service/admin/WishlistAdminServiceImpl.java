package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistAdminServiceImpl implements WishlistAdminService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<WishlistAdminListResponse> findAll(Pageable pageable) {
        List<Wishlist> allItems = wishlistRepository.findAll();

        Map<String, List<Wishlist>> grouped = allItems.stream()
                .filter(w -> w.getCustomerId() != null)
                .collect(Collectors.groupingBy(Wishlist::getCustomerId));

        List<WishlistAdminListResponse> responses = grouped.entrySet().stream().map(entry -> {
            String customerId = entry.getKey();
            List<Wishlist> items = entry.getValue();

            String customerName = customerRepository.findById(customerId)
                    .map(c -> c.getFirstname() + " " + c.getLastname())
                    .orElse("Unknown Customer");

            List<String> productNames = items.stream()
                    .map(Wishlist::getProductId)
                    .map(productRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(Product::getTitle)
                    .toList();

            return new WishlistAdminListResponse(customerId, customerName, productNames);
        }).collect(Collectors.toList());

        responses.sort(Comparator.comparing(WishlistAdminListResponse::getCustomerName));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());

        if (start > responses.size()) {
            return new PageImpl<>(List.of(), pageable, responses.size());
        }

        List<WishlistAdminListResponse> pageContent = responses.subList(start, end);
        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    public WishlistAdminDto create(WishlistCreateRequest request) {
        // Check if already exists
        Optional<Wishlist> existing = wishlistRepository.findByCustomerIdAndProductId(request.getCustomerId(), request.getProductId());
        if (existing.isPresent()) {
             throw new ProductException.Conflict("Wishlist item already exists");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setCustomerId(request.getCustomerId());
        wishlist.setProductId(request.getProductId());
        wishlistRepository.save(wishlist);
        return wishlistMapper.toAdminDto(wishlist);
    }

    @Override
    public void delete(String id) {
        wishlistRepository.deleteById(id);
    }
}
