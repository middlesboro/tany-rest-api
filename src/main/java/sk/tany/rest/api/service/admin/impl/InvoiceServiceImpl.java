package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.client.TanyFeaturesClient;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.dto.features.InvoiceDataDto;
import sk.tany.rest.api.exception.InvoiceException;
import sk.tany.rest.api.exception.OrderException;
import sk.tany.rest.api.service.admin.InvoiceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final OrderRepository orderRepository;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final ProductRepository productRepository;
    private final TanyFeaturesClient tanyFeaturesClient;

    @Override
    public byte[] generateInvoice(String orderId) {
        Order order = getOrder(orderId);
        InvoiceDataDto data = buildInvoiceData(order);
        try {
            return tanyFeaturesClient.generateInvoice(data);
        } catch (Exception e) {
            throw new InvoiceException("Failed to generate invoice via tany-features", e);
        }
    }

    @Override
    public byte[] generateCreditNote(String orderId) {
        Order order = getOrder(orderId);
        InvoiceDataDto data = buildInvoiceData(order);
        try {
            return tanyFeaturesClient.generateCreditNote(data);
        } catch (Exception e) {
            throw new InvoiceException("Failed to generate credit note via tany-features", e);
        }
    }

    private Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with id: " + orderId));
    }

    private InvoiceDataDto buildInvoiceData(Order order) {
        InvoiceDataDto dto = new InvoiceDataDto();

        // Order mapping
        dto.setOrderIdentifier(order.getOrderIdentifier());
        dto.setCreateDate(order.getCreateDate());
        dto.setPaymentNotificationDate(order.getPaymentNotificationDate());
        dto.setEmail(order.getEmail());
        dto.setPhone(order.getPhone());
        if (order.getStatus() != null) dto.setStatus(order.getStatus().name());
        dto.setNote(order.getNote());

        if (order.getInvoiceAddress() != null) {
            dto.setBillingFirstname(order.getFirstname());
            dto.setBillingLastname(order.getLastname());
            dto.setBillingStreet(order.getInvoiceAddress().getStreet());
            dto.setBillingCity(order.getInvoiceAddress().getCity());
            dto.setBillingZip(order.getInvoiceAddress().getZip());
            dto.setBillingCountry(order.getInvoiceAddress().getCountry());
        }

        if (order.getDeliveryAddress() != null) {
            dto.setDeliveryFirstname(order.getFirstname());
            dto.setDeliveryLastname(order.getLastname());
            dto.setDeliveryStreet(order.getDeliveryAddress().getStreet());
            dto.setDeliveryCity(order.getDeliveryAddress().getCity());
            dto.setDeliveryZip(order.getDeliveryAddress().getZip());
            dto.setDeliveryCountry(order.getDeliveryAddress().getCountry());
        }

        if (order.getPriceBreakDown() != null) {
            dto.setTotalPrice(order.getPriceBreakDown().getTotalPrice());
        }

        // Shop Settings
        ShopSettings settings = shopSettingsRepository.getFirstShopSettings();
        if (settings != null) {
            dto.setCompanyName(settings.getOrganizationName());
            dto.setShopIco(settings.getIco());
            dto.setShopDic(settings.getDic());
            dto.setShopIcdph(settings.getVatNumber());
            dto.setShopBankAccount(settings.getBankAccount());
            dto.setShopBankName(settings.getBankName());
            dto.setShopIban(settings.getBankAccount()); // Assuming this is IBAN
            dto.setShopSwift(settings.getBankBic());
            dto.setShopEmail(settings.getShopEmail());
            dto.setShopPhone(settings.getShopPhoneNumber());
            dto.setShopWebsite("Tany.sk");

            dto.setShopStreet(settings.getShopStreet());
            dto.setShopCity(settings.getShopCity());
            dto.setShopZip(settings.getShopZip());
            dto.setShopCountry(settings.getDefaultCountry());
        }

        // Carrier & Payment Names
        if (order.getCarrierId() != null) {
            carrierRepository.findById(order.getCarrierId()).ifPresent(c -> dto.setCarrierName(c.getName()));
        }
        if (order.getPaymentId() != null) {
            paymentRepository.findById(order.getPaymentId()).ifPresent(p -> dto.setPaymentName(p.getName()));
        }

        // Items mapping
        if (order.getPriceBreakDown() != null && order.getPriceBreakDown().getItems() != null) {
            List<String> productIds = order.getPriceBreakDown().getItems().stream()
                    .filter(i -> i.getType() == PriceItemType.PRODUCT)
                    .map(PriceItem::getId)
                    .collect(Collectors.toList());

            Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            List<InvoiceDataDto.InvoiceItemDto> itemDtos = new ArrayList<>();
            for (PriceItem i : order.getPriceBreakDown().getItems()) {
                InvoiceDataDto.InvoiceItemDto iDto = new InvoiceDataDto.InvoiceItemDto();
                iDto.setId(i.getId());
                iDto.setTitle(i.getName());
                iDto.setPrice(i.getPriceWithVat());
                iDto.setQuantity(i.getQuantity());

                if (i.getType() == PriceItemType.PRODUCT) {
                    Product p = productMap.get(i.getId());
                    if (p != null) {
                        iDto.setProductId(p.getId());
                        iDto.setProductCode(p.getProductCode());
                    }
                }
                itemDtos.add(iDto);
            }
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
