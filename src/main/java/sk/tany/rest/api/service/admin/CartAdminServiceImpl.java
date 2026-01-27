package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartItem;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.admin.cart.list.CartAdminListResponse;
import sk.tany.rest.api.mapper.CartMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CartAdminServiceImpl implements CartAdminService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Page<CartAdminListResponse> findAll(String cartId, Long orderIdentifier, String customerName, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

        // OPTIMIZATION 1: Filter by Order Identifier
        if (orderIdentifier != null) {
            return findAllByOrderIdentifier(cartId, orderIdentifier, customerName, dateFrom, dateTo, pageable);
        }

        // OPTIMIZATION 2: Simple Listing (No expensive filtering/sorting)
        if (customerName == null && !isSortedByDerivedField(pageable.getSort())) {
            return findAllSimple(cartId, dateFrom, dateTo, pageable);
        }

        // FALLBACK: Full In-Memory Join (Required for complex filtering/sorting)
        // Note: This loads all entities into memory. This is acceptable for the current "In-Memory Repository" architecture
        // but would require refactoring for a production SQL database with large datasets.
        return findAllFullJoin(cartId, null, customerName, dateFrom, dateTo, pageable);
    }

    private Page<CartAdminListResponse> findAllByOrderIdentifier(String cartId, Long orderIdentifier, String customerName, LocalDate createDateFrom, LocalDate createDateTo, Pageable pageable) {
        // Find orders matching identifier (usually 0 or 1)
        List<Order> orders = orderRepository.findAll(orderIdentifier, null, null, null, null, null, null, null, Pageable.unpaged()).getContent();

        List<CartAdminListResponse> result = new ArrayList<>();
        for (Order order : orders) {
             if (order.getCartId() != null) {
                 cartRepository.findById(order.getCartId()).ifPresent(cart -> {
                     CartAdminListResponse resp = mapToResponse(cart, order);
                     // Apply other filters (cartId, customerName, date)
                     if (matchesFilters(resp, cartId, customerName, createDateFrom, createDateTo)) {
                         result.add(resp);
                     }
                 });
             }
        }

        return new PageImpl<>(result, pageable, result.size());
    }

    private Page<CartAdminListResponse> findAllSimple(String cartId, LocalDate createDateFrom, LocalDate createDateTo, Pageable pageable) {
        Stream<Cart> stream = cartRepository.findAll().stream();

        if (cartId != null) {
            stream = stream.filter(c -> c.getId() != null && c.getId().contains(cartId));
        }
        if (createDateFrom != null) {
            stream = stream.filter(c -> c.getCreateDate() != null && !createLocalDateFromInstant(c.getCreateDate()).isBefore(createDateFrom));
        }
        if (createDateTo != null) {
            stream = stream.filter(c -> c.getCreateDate() != null && !createLocalDateFromInstant(c.getCreateDate()).isAfter(createDateTo));
        }

        List<Cart> filteredCarts = stream.collect(Collectors.toList());

        // Sort Carts
        if (pageable.getSort().isSorted()) {
            Comparator<Cart> comparator = null;
            for (Sort.Order order : pageable.getSort()) {
                Comparator<Cart> current = null;
                switch (order.getProperty()) {
                    case "cartId": current = Comparator.comparing(Cart::getId, Comparator.nullsLast(String::compareTo)); break;
                    case "createDate": current = Comparator.comparing(Cart::getCreateDate, Comparator.nullsLast(Instant::compareTo)); break;
                    case "updateDate": current = Comparator.comparing(Cart::getUpdateDate, Comparator.nullsLast(Instant::compareTo)); break;
                }
                if (current != null) {
                    if (order.isDescending()) current = current.reversed();
                    if (comparator == null) comparator = current;
                    else comparator = comparator.thenComparing(current);
                }
            }
             if (comparator != null) {
                filteredCarts.sort(comparator);
            }
        } else {
             filteredCarts.sort(Comparator.comparing(Cart::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // Paginate
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCarts.size());
        if (start > filteredCarts.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, filteredCarts.size());
        }

        List<Cart> pageCarts = filteredCarts.subList(start, end);

        // Enrich
        List<CartAdminListResponse> content = pageCarts.stream()
                .map(cart -> {
                    // Fetch related data for this single item
                    Order order = orderRepository.findByCartId(cart.getId()).orElse(null);
                    return mapToResponse(cart, order);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, filteredCarts.size());
    }

    private Page<CartAdminListResponse> findAllFullJoin(String cartId, Long orderIdentifier, String customerName, LocalDate createDateFrom, LocalDate createDateTo, Pageable pageable) {
        // ... Logic from previous implementation ...
        // Fetch all data needed for in-memory join
        List<Cart> allCarts = cartRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        Map<String, Order> ordersByCartId = allOrders.stream()
                .filter(o -> o.getCartId() != null)
                .collect(Collectors.toMap(Order::getCartId, o -> o, (o1, o2) -> o1));

        // Note: For sorting by derived names, we need to resolve all names.
        // We optimize by fetching all and mapping.
        List<Customer> allCustomers = customerRepository.findAll();
        Map<String, Customer> customerMap = allCustomers.stream().collect(Collectors.toMap(Customer::getId, c -> c));

        List<Carrier> allCarriers = carrierRepository.findAll();
        Map<String, String> carrierNames = allCarriers.stream().collect(Collectors.toMap(Carrier::getId, Carrier::getName));

        List<Payment> allPayments = paymentRepository.findAll();
        Map<String, String> paymentNames = allPayments.stream().collect(Collectors.toMap(Payment::getId, Payment::getName));

        Stream<CartAdminListResponse> stream = allCarts.stream().map(cart -> {
            CartAdminListResponse response = mapToResponse(cart, ordersByCartId.get(cart.getId()));

            // Re-resolve names using maps for speed in full join
             String cId = response.getCustomerId();
            if (cId != null && customerMap.containsKey(cId)) {
                Customer c = customerMap.get(cId);
                response.setCustomerName(c.getFirstname() + " " + c.getLastname());
            }

            String carId = cart.getSelectedCarrierId();
            if (carId == null && ordersByCartId.containsKey(cart.getId())) {
                carId = ordersByCartId.get(cart.getId()).getCarrierId();
            }
            if (carId != null) {
                response.setCarrierName(carrierNames.get(carId));
            }

            String payId = cart.getSelectedPaymentId();
            if (payId == null && ordersByCartId.containsKey(cart.getId())) {
                 payId = ordersByCartId.get(cart.getId()).getPaymentId();
            }
            if (payId != null) {
                response.setPaymentName(paymentNames.get(payId));
            }

            return response;
        });

        // Apply filters
        if (cartId != null) {
             stream = stream.filter(r -> r.getCartId() != null && r.getCartId().contains(cartId));
        }
        if (orderIdentifier != null) {
            stream = stream.filter(r -> r.getOrderIdentifier() != null && r.getOrderIdentifier().equals(orderIdentifier));
        }
        if (customerName != null) {
            String lowerName = customerName.toLowerCase();
            stream = stream.filter(r -> r.getCustomerName() != null && r.getCustomerName().toLowerCase().contains(lowerName));
        }
        if (createDateFrom != null) {
            stream = stream.filter(r -> r.getCreateDate() != null && !createLocalDateFromInstant(r.getCreateDate()).isBefore(createDateFrom));
        }
        if (createDateTo != null) {
            stream = stream.filter(r -> r.getCreateDate() != null && !createLocalDateFromInstant(r.getCreateDate()).isAfter(createDateTo));
        }

        List<CartAdminListResponse> filtered = stream.collect(Collectors.toList());

        // Sort
        if (pageable.getSort().isSorted()) {
            Comparator<CartAdminListResponse> comparator = null;
            for (Sort.Order order : pageable.getSort()) {
                Comparator<CartAdminListResponse> current = null;
                switch (order.getProperty()) {
                    case "cartId": current = Comparator.comparing(CartAdminListResponse::getCartId, Comparator.nullsLast(String::compareTo)); break;
                    case "orderIdentifier": current = Comparator.comparing(CartAdminListResponse::getOrderIdentifier, Comparator.nullsLast(Long::compareTo)); break;
                    case "customerName": current = Comparator.comparing(CartAdminListResponse::getCustomerName, Comparator.nullsLast(String::compareTo)); break;
                    case "price": current = Comparator.comparing(CartAdminListResponse::getPrice, Comparator.nullsLast(BigDecimal::compareTo)); break;
                    case "createDate": current = Comparator.comparing(CartAdminListResponse::getCreateDate, Comparator.nullsLast(Instant::compareTo)); break;
                    default: break;
                }
                if (current != null) {
                    if (order.isDescending()) current = current.reversed();
                    if (comparator == null) comparator = current;
                    else comparator = comparator.thenComparing(current);
                }
            }
            if (comparator != null) {
                filtered.sort(comparator);
            }
        } else {
             filtered.sort(Comparator.comparing(CartAdminListResponse::getCreateDate, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<CartAdminListResponse> pageContent;
        if (start > filtered.size()) {
            pageContent = Collections.emptyList();
        } else {
            pageContent = filtered.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private CartAdminListResponse mapToResponse(Cart cart, Order order) {
        CartAdminListResponse response = new CartAdminListResponse();
        response.setCartId(cart.getId());
        response.setCreateDate(cart.getCreateDate());
        response.setUpdateDate(cart.getUpdateDate());
        response.setCustomerId(cart.getCustomerId());

        if (order != null) {
            response.setOrderIdentifier(order.getOrderIdentifier());
            response.setPrice(order.getFinalPrice());
            if (response.getCustomerId() == null) {
                 response.setCustomerId(order.getCustomerId());
            }
        } else {
            BigDecimal total = BigDecimal.ZERO;
            if (cart.getItems() != null) {
                for (CartItem item : cart.getItems()) {
                    if (item.getPrice() != null && item.getQuantity() != null) {
                        total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }
            response.setPrice(total);
        }

        // Resolve Name (Lazy/Single) - Used in Simple Path.
        // Full Join Path overwrites this with Map-based resolution.
        if (response.getCustomerName() == null) {
            String cId = response.getCustomerId();
            if (cId != null) {
                customerRepository.findById(cId).ifPresent(c ->
                    response.setCustomerName(c.getFirstname() + " " + c.getLastname()));
            }
            if (response.getCustomerName() == null && order != null && order.getFirstname() != null) {
                response.setCustomerName(order.getFirstname() + " " + order.getLastname());
            }
            if (response.getCustomerName() == null && cart.getFirstname() != null) {
                response.setCustomerName(cart.getFirstname() + " " + cart.getLastname());
            }
        }

        // Carrier/Payment (Lazy/Single)
        if (response.getCarrierName() == null) {
             String carId = cart.getSelectedCarrierId();
            if (carId == null && order != null) {
                carId = order.getCarrierId();
            }
            if (carId != null) {
                carrierRepository.findById(carId).ifPresent(c -> response.setCarrierName(c.getName()));
            }
        }

        if (response.getPaymentName() == null) {
            String payId = cart.getSelectedPaymentId();
            if (payId == null && order != null) {
                payId = order.getPaymentId();
            }
            if (payId != null) {
                paymentRepository.findById(payId).ifPresent(p -> response.setPaymentName(p.getName()));
            }
        }

        return response;
    }

    private boolean matchesFilters(CartAdminListResponse r, String cartId, String customerName, LocalDate from, LocalDate to) {


        if (cartId != null && (r.getCartId() == null || !r.getCartId().contains(cartId))) return false;
        if (customerName != null && (r.getCustomerName() == null || !r.getCustomerName().toLowerCase().contains(customerName.toLowerCase()))) return false;
        if (from != null && (r.getCreateDate() == null || createLocalDateFromInstant(r.getCreateDate()).isBefore(from))) return false;
        if (to != null && (r.getCreateDate() == null || createLocalDateFromInstant(r.getCreateDate()).isAfter(to))) return false;
        return true;
    }

    private boolean isSortedByDerivedField(Sort sort) {
        if (!sort.isSorted()) return false;
        return sort.stream().anyMatch(order ->
            Set.of("orderIdentifier", "price", "customerName", "carrierName", "paymentName")
               .contains(order.getProperty())
        );
    }

    // ... existing findById, deleteById, save, patch ...

    @Override
    public Optional<CartDto> findById(String id) {
        return cartRepository.findById(id).map(cartMapper::toDto);
    }

    @Override
    public void deleteById(String id) {
        cartRepository.deleteById(id);
    }

    @Override
    public CartDto save(CartDto cartDto) {
        Cart cart;
        if (cartDto.getCartId() != null) {
            cart = cartRepository.findById(cartDto.getCartId()).orElse(new Cart());
        } else {
            cart = new Cart();
        }
        cartMapper.updateEntityFromDto(cartDto, cart);
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Override
    public CartDto patch(String id, sk.tany.rest.api.dto.admin.cart.patch.CartPatchRequest patchDto) {
        var cart = cartRepository.findById(id).orElseThrow(() -> new RuntimeException("Cart not found"));
        cartMapper.updateEntityFromPatch(patchDto, cart);
        return cartMapper.toDto(cartRepository.save(cart));
    }

    private LocalDate createLocalDateFromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
