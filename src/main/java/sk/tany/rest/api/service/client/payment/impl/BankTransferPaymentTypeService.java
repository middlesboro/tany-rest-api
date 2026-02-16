package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;
import sk.tany.rest.api.service.payment.PayBySquareService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankTransferPaymentTypeService implements PaymentTypeService {

    private final PayBySquareService payBySquareService;
    private final ShopSettingsRepository shopSettingsRepository;

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.BANK_TRANSFER;
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment) {
        ShopSettings settings = getShopSettings();
        return PaymentInfoDto.builder()
                .iban(settings.getBankAccount())
                .swift(settings.getBankBic())
                .variableSymbol(getVariableSymbol(order))
                .qrCode(generateQrCodeBase64(order, settings))
                .paymentLink(generatePaymeLink(order, settings))
                .build();
    }

    private ShopSettings getShopSettings() {
        return shopSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(ShopSettings::new);
    }

    private String getVariableSymbol(OrderDto order) {
        return order.getOrderIdentifier() != null ? String.valueOf(order.getOrderIdentifier()) : null;
    }

    private String generateQrCodeBase64(OrderDto order, ShopSettings settings) {
        try {
            return generateQrCode(order, settings);
        } catch (Exception e) {
            log.error("Error generating QR code for order {}", order.getId(), e);
            return null;
        }
    }

    private String generatePaymeLink(OrderDto order, ShopSettings settings) {
        try {
            String vs = getVariableSymbol(order);
            if (vs == null) {
                return null;
            }
            String msg = "Objednavka VS: " + vs;

            String organizationName = settings.getOrganizationName();
            if (organizationName == null) {
                organizationName = "Tany.sk"; // Fallback if settings missing
            }

            return "https://payme.sk?V=1" +
                    "&IBAN=" + (settings.getBankAccount() != null ? settings.getBankAccount().replace(" ", "") : "") +
                    "&AM=" + String.format(Locale.US, "%.2f", order.getFinalPrice()) +
                    "&CC=EUR" +
                    "&MSG=" + URLEncoder.encode(msg, StandardCharsets.UTF_8) +
                    "&CN=" + URLEncoder.encode(organizationName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error generating Payme link for order {}", order.getId(), e);
            return null;
        }
    }

    private String generateQrCode(OrderDto order, ShopSettings settings) {
        return payBySquareService.generateQrCode(
                order.getFinalPrice(),
                "EUR",
                getVariableSymbol(order),
                null,
                null,
                null,
                null,
                settings.getBankAccount(),
                settings.getBankBic(),
                settings.getOrganizationName()
        );
    }
}
