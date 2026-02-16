package sk.tany.rest.api.service.client.payment.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.payment.PayBySquareService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BankTransferPaymentTypeServiceTest {

    private PayBySquareService payBySquareService;
    private ShopSettingsRepository shopSettingsRepository;
    private BankTransferPaymentTypeService service;

    @BeforeEach
    void setUp() {
        payBySquareService = Mockito.mock(PayBySquareService.class);
        shopSettingsRepository = Mockito.mock(ShopSettingsRepository.class);
        service = new BankTransferPaymentTypeService(payBySquareService, shopSettingsRepository);
    }

    @Test
    void getPaymentInfo_shouldReturnCorrectInfo() {
        OrderDto order = new OrderDto();
        order.setFinalPrice(BigDecimal.valueOf(100.0));
        order.setOrderIdentifier(12345L);

        PaymentDto payment = new PaymentDto();

        ShopSettings settings = new ShopSettings();
        settings.setBankAccount("SK1234567890");
        settings.setBankBic("SUBASKBA");
        settings.setOrganizationName("Tany Shop");

        when(shopSettingsRepository.findAll()).thenReturn(Collections.singletonList(settings));
        when(payBySquareService.generateQrCode(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("mockQrCodeBase64");

        PaymentInfoDto result = service.getPaymentInfo(order, payment);

        assertEquals("SK1234567890", result.getIban());
        assertEquals("SUBASKBA", result.getSwift());
        assertEquals("12345", result.getVariableSymbol());
        assertEquals("mockQrCodeBase64", result.getQrCode());

        Mockito.verify(payBySquareService).generateQrCode(
                eq(BigDecimal.valueOf(100.0)),
                eq("EUR"),
                eq("12345"),
                any(),
                any(),
                any(),
                any(),
                eq("SK1234567890"),
                eq("SUBASKBA"),
                eq("Tany Shop")
        );
    }
}
