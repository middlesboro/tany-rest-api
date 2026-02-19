package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartItem;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
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
import java.util.regex.Pattern;
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
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<CartAdminListResponse> findAll(String cartId, Long orderIdentifier, String customerName, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        if (orderIdentifier != null) {
            return findAllByOrderIdentifier(cartId, orderIdentifier, customerName, dateFrom, dateTo, pageable);
        }

        Query query = new Query();

        if (cartId != null && !cartId.isEmpty()) {
            query.addCriteria(Criteria.where("id").regex(Pattern.quote(cartId), "i"));
        }

        if (customerName != null && !customerName.isEmpty()) {
            String[] parts = customerName.split("\\s+");
            List<Criteria> criteriaList = new ArrayList<>();
            for (String part : parts) {
                criteriaList.add(new Criteria().orOperator(
                        Criteria.where("firstname").regex(Pattern.quote(part), "i"),
                        Criteria.where("lastname").regex(Pattern.quote(part), "i")
                ));
            }
            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        if (dateFrom != null || dateTo != null) {
            Criteria createDateCriteria = Criteria.where("createDate");
            if (dateFrom != null) {
                createDateCriteria.gte(dateFrom.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (dateTo != null) {
                createDateCriteria.lte(dateTo.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant());
            }
            query.addCriteria(createDateCriteria);
        }

        long count = mongoTemplate.count(query, Cart.class);

        // Sort logic replacement
        List<Sort.Order> sortOrders = new ArrayList<>();
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                if ("customerName".equals(order.getProperty())) {
                    sortOrders.add(new Sort.Order(order.getDirection(), "lastname"));
                    sortOrders.add(new Sort.Order(order.getDirection(), "firstname"));
                } else if ("price".equals(order.getProperty()) || "orderIdentifier".equals(order.getProperty()) || "carrierName".equals(order.getProperty()) || "paymentName".equals(order.getProperty())) {
                    // Ignore derived fields for DB sort to avoid errors
                } else {
                    sortOrders.add(order);
                }
            }
        }

        if (sortOrders.isEmpty()) {
            // Default sort if none provided or all filtered out
            sortOrders.add(new Sort.Order(Sort.Direction.DESC, "createDate"));
        }

        query.with(org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortOrders)));

        List<Cart> carts = mongoTemplate.find(query, Cart.class);

        // Fetch related Orders
        List<String> cartIds = carts.stream().map(Cart::getId).collect(Collectors.toList());
        Map<String, Order> ordersByCartId = orderRepository.findByCartIdIn(cartIds).stream()
                .filter(o -> o.getCartId() != null)
                .collect(Collectors.toMap(Order::getCartId, o -> o, (o1, o2) -> o1));

        List<CartAdminListResponse> content = carts.stream()
                .map(cart -> mapToResponse(cart, ordersByCartId.get(cart.getId())))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, count);
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

        // Resolve Name (Lazy/Single)
        if (response.getCustomerName() == null) {
            if (cart.getFirstname() != null) {
                response.setCustomerName(cart.getFirstname() + " " + cart.getLastname());
            }
            // Fallback to Order or Customer if not in Cart (for backward compatibility)
            if (response.getCustomerName() == null && order != null && order.getFirstname() != null) {
                 response.setCustomerName(order.getFirstname() + " " + order.getLastname());
            }
            if (response.getCustomerName() == null) {
                 String cId = response.getCustomerId();
                if (cId != null) {
                    customerRepository.findById(cId).ifPresent(c ->
                        response.setCustomerName(c.getFirstname() + " " + c.getLastname()));
                }
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
