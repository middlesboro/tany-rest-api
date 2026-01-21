package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

        if (customerDto != null) {
            if (cartDto.getFirstname() == null || cartDto.getFirstname().isEmpty()) {
                cartDto.setFirstname(customerDto.getFirstname());
            }
            if (cartDto.getLastname() == null || cartDto.getLastname().isEmpty()) {
                cartDto.setLastname(customerDto.getLastname());
            }
            if (cartDto.getEmail() == null || cartDto.getEmail().isEmpty()) {
                cartDto.setEmail(customerDto.getEmail());
            }
            if (cartDto.getPhone() == null || cartDto.getPhone().isEmpty()) {
                cartDto.setPhone(customerDto.getPhone());
            }
            if (cartDto.getInvoiceAddress() == null) {
                cartDto.setInvoiceAddress(customerDto.getInvoiceAddress());
            }
            if (cartDto.getDeliveryAddress() == null) {
                cartDto.setDeliveryAddress(customerDto.getDeliveryAddress());
            } // save just in case if something was changed
            cartDto = cartService.save(cartDto);
        }

        CustomerContextCartDto customerContextCartDto = new CustomerContextCartDto();
        customerContextCartDto.setCartId(cartDto.getCartId());
        customerContextCartDto.setCustomerId(cartDto.getCustomerId());

        List<ProductClientDto> products = new ArrayList<>();
        if (cartDto.getItems() != null && !cartDto.getItems().isEmpty()) {
            List<String> productIds = cartDto.getItems().stream().map(CartItem::getProductId).toList();
            List<ProductClientDto> fetchedProducts = productService.findAllByIds(productIds);

            for (ProductClientDto product : fetchedProducts) {
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
                .map(p -> (p.getWeight() != null ? p.getWeight() : BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(p.getQuantity() != null ? p.getQuantity() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CarrierDto> carriers = carrierService.findAll(Pageable.unpaged()).getContent();
        CartDto finalCartDto = cartDto;
        carriers.forEach(carrier -> {
            carrier.setSelected(carrier.getId().equals(finalCartDto.getSelectedCarrierId()));
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
        carriers = carriers.stream().sorted(Comparator.comparing(CarrierDto::getOrder)).toList();
        if (carriers.stream().noneMatch(CarrierDto::isSelected) && !carriers.isEmpty()) {
            carriers.getFirst().setSelected(true);
        }
        customerContextCartDto.setCarriers(carriers);

        List<PaymentDto> payments = paymentService.findAll(Pageable.unpaged()).getContent();
        CartDto finalCartDto1 = cartDto;
        payments.forEach(payment -> payment.setSelected(payment.getId().equals(finalCartDto1.getSelectedPaymentId())));
        if (payments.stream().noneMatch(PaymentDto::isSelected) && !payments.isEmpty()) {
            payments.getFirst().setSelected(true);
        }
        customerContextCartDto.setPayments(payments);

        customerContextCartDto.setEmail(cartDto.getEmail());
        customerContextCartDto.setPhone(cartDto.getPhone());
        customerContextCartDto.setFirstname(cartDto.getFirstname());
        customerContextCartDto.setLastname(cartDto.getLastname());
        customerContextCartDto.setDeliveryAddress(cartDto.getDeliveryAddress());
        customerContextCartDto.setInvoiceAddress(cartDto.getInvoiceAddress());
        customerContextCartDto.setSelectedPickupPointId(cartDto.getSelectedPickupPointId());
        customerContextCartDto.setSelectedPickupPointName(cartDto.getSelectedPickupPointName());
        customerContextCartDto.setAppliedDiscounts(cartDto.getAppliedDiscounts());
        customerContextCartDto.setTotalDiscount(cartDto.getTotalDiscount());
        customerContextCartDto.setFinalPrice(cartDto.getFinalPrice());
        customerContextCartDto.setFreeShipping(cartDto.isFreeShipping());

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

    @Override
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (customerDto.getFirstname() != null) {
            customer.setFirstname(customerDto.getFirstname());
        }
        if (customerDto.getLastname() != null) {
            customer.setLastname(customerDto.getLastname());
        }
        // Email update might require verification in a real scenario, but based on requirements "edit all fields", we allow it.
        // However, if email changes, the principal might become invalid for future requests if not handled.
        // But simply updating the field is what is asked.
        if (customerDto.getEmail() != null) {
            customer.setEmail(customerDto.getEmail());
        }
        if (customerDto.getInvoiceAddress() != null) {
            customer.setInvoiceAddress(customerMapper.toEntity(customerDto.getInvoiceAddress()));
        }
        if (customerDto.getDeliveryAddress() != null) {
            customer.setDeliveryAddress(customerMapper.toEntity(customerDto.getDeliveryAddress()));
        }

        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    public CustomerDto getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .map(customerMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }
}
