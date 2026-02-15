package sk.tany.rest.api.service.client.payment.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.payment.PayBySquareService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BankTransferPaymentTypeServiceTest {

    private PayBySquareService payBySquareService;
    private BankTransferPaymentTypeService service;

    @BeforeEach
    void setUp() {
        payBySquareService = Mockito.mock(PayBySquareService.class);
        service = new BankTransferPaymentTypeService(payBySquareService);
        ReflectionTestUtils.setField(service, "iban", "SK1234567890");
        ReflectionTestUtils.setField(service, "bic", "SUBASKBA");
    }

    @Test
    void getPaymentInfo_shouldReturnCorrectInfo() {
        OrderDto order = new OrderDto();
        order.setFinalPrice(BigDecimal.valueOf(100.0));
        order.setOrderIdentifier(12345L);

        PaymentDto payment = new PaymentDto();

        when(payBySquareService.generateQrCode(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("mockQrCodeBase64");

        PaymentInfoDto result = service.getPaymentInfo(order, payment);

        assertEquals("SK1234567890", result.getIban());
        assertEquals("SUBASKBA", result.getSwift());
        assertEquals("12345", result.getVariableSymbol());
        assertEquals("mockQrCodeBase64", result.getQrCode());

        Mockito.verify(payBySquareService).generateQrCode(
                BigDecimal.valueOf(100.0),
                "EUR",
                "12345",
                null,
                null,
                null,
                null,
                "SK1234567890",
                "SUBASKBA"
        );
    }
}
