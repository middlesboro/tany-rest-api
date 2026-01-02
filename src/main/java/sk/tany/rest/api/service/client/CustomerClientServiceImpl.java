package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.*;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerClientServiceImpl implements CustomerClientService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CartClientService cartService;
    private final ProductClientService productService;
    private final CarrierClientService carrierService;
    private final PaymentClientService paymentService;

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
        customerContextCartDto.setTotalProductPrice(
                products.stream()
                        .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        BigDecimal totalWeight = products.stream()
                .map(p -> (p.getWeight() != null ? p.getWeight() : BigDecimal.ZERO).multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CarrierDto> carriers = carrierService.findAll(Pageable.unpaged()).getContent();
        carriers.forEach(carrier -> {
            carrier.setSelected(carrier.getId().equals(cartDto.getSelectedCarrierId()));
            if (carrier.getRanges() != null) {
                carrier.getRanges().stream()
                        .filter(range ->
                                (range.getWeightFrom() == null || totalWeight.compareTo(range.getWeightFrom()) >= 0) &&
                                        (range.getWeightTo() == null || totalWeight.compareTo(range.getWeightTo()) <= 0)
                        )
                        .findFirst()
                        .ifPresent(range -> carrier.setPrice(range.getPrice()));
            }
            carrier.setRanges(null);
        });
        customerContextCartDto.setCarriers(carriers);

        List<PaymentDto> payments = paymentService.findAll(Pageable.unpaged()).getContent();
        payments.forEach(payment -> payment.setSelected(payment.getId().equals(cartDto.getSelectedPaymentId())));
        customerContextCartDto.setPayments(payments);

        return new CustomerContextDto(customerDto, customerContextCartDto);
    }

    public CustomerDto findByEmail(String email) {
        return customerRepository.findByEmail(email).map(customerMapper::toDto).orElse(null);
    }

    public CustomerDto save(CustomerDto customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    public Optional<CustomerDto> findById(String id) {
        return customerRepository.findById(id).map(customerMapper::toDto);
    }
}
