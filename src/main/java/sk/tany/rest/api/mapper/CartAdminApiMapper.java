package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateRequest;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateResponse;
import sk.tany.rest.api.dto.admin.cart.get.CartAdminGetResponse;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateResponse;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public abstract class CartAdminApiMapper {

    @Autowired
    protected OrderRepository orderRepository;
    @Autowired
    protected CustomerRepository customerRepository;
    @Autowired
    protected CarrierRepository carrierRepository;
    @Autowired
    protected PaymentRepository paymentRepository;

    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    public abstract CartDto toDto(CartAdminCreateRequest request);

    public abstract CartAdminCreateResponse toCreateResponse(CartDto dto);

    @Mapping(target = "priceBreakDown", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "orderIdentifier", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "carrierName", ignore = true)
    @Mapping(target = "paymentName", ignore = true)
    protected abstract CartAdminGetResponse mapBase(CartDto dto);

    public CartAdminGetResponse toGetResponse(CartDto dto) {
        CartAdminGetResponse response = mapBase(dto);

        // Populate new fields
        Order order = null;
        if (dto.getCartId() != null) {
            order = orderRepository.findByCartId(dto.getCartId()).orElse(null);
        }

        // Order Identifier
        if (order != null) {
            response.setOrderIdentifier(order.getOrderIdentifier());
        }

        // Price
        if (order != null && order.getFinalPrice() != null) {
            response.setPrice(order.getFinalPrice());
        } else if (dto.getFinalPrice() != null) {
            response.setPrice(dto.getFinalPrice());
        } else {
            BigDecimal total = BigDecimal.ZERO;
            if (dto.getItems() != null) {
                for (CartItem item : dto.getItems()) {
                    if (item.getPrice() != null && item.getQuantity() != null) {
                        total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }
            response.setPrice(total);
        }

        // PriceBreakDown
        if (order != null && order.getPriceBreakDown() != null) {
            response.setPriceBreakDown(order.getPriceBreakDown());
        } else {
            response.setPriceBreakDown(dto.getPriceBreakDown());
        }

        // Customer Name
        if (dto.getCustomerId() != null) {
            customerRepository.findById(dto.getCustomerId()).ifPresent(c ->
                    response.setCustomerName((c.getFirstname() != null ? c.getFirstname() : "") + " " + (c.getLastname() != null ? c.getLastname() : "")));
        }
        if (response.getCustomerName() == null || response.getCustomerName().trim().isEmpty()) {
            if (dto.getFirstname() != null || dto.getLastname() != null) {
                response.setCustomerName((dto.getFirstname() != null ? dto.getFirstname() : "") + " " + (dto.getLastname() != null ? dto.getLastname() : ""));
            }
        }
        if (response.getCustomerName() != null) {
            response.setCustomerName(response.getCustomerName().trim());
        }

        // Carrier Name
        String carrierId = dto.getSelectedCarrierId();
        if (carrierId == null && order != null) {
            carrierId = order.getCarrierId();
        }
        if (carrierId != null) {
            carrierRepository.findById(carrierId).ifPresent(c -> response.setCarrierName(c.getName()));
        }

        // Payment Name
        String paymentId = dto.getSelectedPaymentId();
        if (paymentId == null && order != null) {
            paymentId = order.getPaymentId();
        }
        if (paymentId != null) {
            paymentRepository.findById(paymentId).ifPresent(p -> response.setPaymentName(p.getName()));
        }

        return response;
    }

    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    public abstract CartDto toDto(CartAdminUpdateRequest request);

    public abstract CartAdminUpdateResponse toUpdateResponse(CartDto dto);
}
