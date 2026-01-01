package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.*;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CartService cartService;
    private final ProductService productService;

    public CustomerContextDto getCustomerContext(String cartId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomerDto customerDto = null;
        String customerId = null;

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String) {
            String email = (String) authentication.getPrincipal();
            customerDto = findByEmail(email);
            if (customerDto != null) {
                customerId = customerDto.getId();
            }
        }

        CartDto cartDto = cartService.getOrCreateCart(cartId, customerId);

        CustomerContextCartDto customerContextCartDto = new CustomerContextCartDto();
        customerContextCartDto.setCartId(cartDto.getCartId());
        customerContextCartDto.setCustomerId(cartDto.getCustomerId());

        List<ProductDto> products = new ArrayList<>();
        if (cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
            List<String> productIds = cartDto.getItems().stream().map(CartItem::getProductId).toList();
            List<ProductDto> fetchedProducts = productService.findAllByIds(productIds);

            for (ProductDto product : fetchedProducts) {
                cartDto.getItems().stream()
                        .filter(item -> item.getProductId().equals(product.getId()))
                        .findFirst()
                        .ifPresent(item -> product.setQuantity(item.getQuantity()));
                products.add(product);
            }
        }
        customerContextCartDto.setProducts(products);

        return new CustomerContextDto(customerDto, customerContextCartDto);
    }

    public CustomerDto findByEmail(String email) {
        return customerRepository.findByEmail(email).map(customerMapper::toDto).orElse(null);
    }

    public CustomerDto save(CustomerDto customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream().map(customerMapper::toDto).toList();
    }

    public Page<CustomerDto> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDto);
    }

    public Optional<CustomerDto> findById(String id) {
        return customerRepository.findById(id).map(customerMapper::toDto);
    }

    public void deleteById(String id) {
        customerRepository.deleteById(id);
    }
}
