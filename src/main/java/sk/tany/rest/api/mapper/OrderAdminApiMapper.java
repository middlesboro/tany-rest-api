package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateRequest;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateResponse;
import sk.tany.rest.api.dto.admin.order.get.OrderAdminGetResponse;
import sk.tany.rest.api.dto.admin.order.list.OrderAdminListResponse;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateResponse;

@Mapper(componentModel = "spring")
public abstract class OrderAdminApiMapper {

    @Autowired
    protected CustomerRepository customerRepository;
    @Autowired
    protected CarrierRepository carrierRepository;
    @Autowired
    protected PaymentRepository paymentRepository;

    @Mapping(target = "id", ignore = true)
    public abstract OrderDto toDto(OrderAdminCreateRequest request);
    public abstract OrderAdminCreateResponse toCreateResponse(OrderDto dto);

    public abstract OrderAdminGetResponse toGetResponse(OrderDto dto);

    @Mapping(target = "customerName", expression = "java(resolveCustomerName(dto.getCustomerId()))")
    @Mapping(target = "carrierName", expression = "java(resolveCarrierName(dto.getCarrierId()))")
    @Mapping(target = "paymentName", expression = "java(resolvePaymentName(dto.getPaymentId()))")
    public abstract OrderAdminListResponse toListResponse(OrderDto dto);

    @Mapping(target = "id", ignore = true)
    public abstract OrderDto toDto(OrderAdminUpdateRequest request);
    public abstract OrderAdminUpdateResponse toUpdateResponse(OrderDto dto);

    @Named("resolveCustomerName")
    protected String resolveCustomerName(String customerId) {
        if (customerId == null) return null;
        return customerRepository.findById(customerId)
                .map(c -> (c.getFirstname() != null ? c.getFirstname() : "") + " " + (c.getLastname() != null ? c.getLastname() : ""))
                .map(String::trim)
                .orElse(null);
    }

    @Named("resolveCarrierName")
    protected String resolveCarrierName(String carrierId) {
        if (carrierId == null) return null;
        return carrierRepository.findById(carrierId)
                .map(c -> c.getName())
                .orElse(null);
    }

    @Named("resolvePaymentName")
    protected String resolvePaymentName(String paymentId) {
        if (paymentId == null) return null;
        return paymentRepository.findById(paymentId)
                .map(p -> p.getName())
                .orElse(null);
    }
}
