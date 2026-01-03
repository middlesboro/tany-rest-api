package sk.tany.rest.api.service.client.payment.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentTypeServiceTest {

    @InjectMocks
    private BankTransferPaymentTypeService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "iban", "SK1234567890");
        ReflectionTestUtils.setField(service, "bic", "TESTBIC");
    }

    @Test
    void getSupportedType() {
        assertEquals(PaymentType.BANK_TRANSFER, service.getSupportedType());
    }

    @Test
    void getPaymentInfo_ShouldGenerateQrCode() {
        OrderDto order = new OrderDto();
        order.setOrderIdentifier(12345L);
        order.setFinalPrice(new BigDecimal("100.50"));

        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentType.BANK_TRANSFER);

        PaymentInfoDto result = service.getPaymentInfo(order, payment);

        assertNotNull(result);
        assertEquals("SK1234567890", result.getIban());
        assertEquals("TESTBIC", result.getSwift());
        assertEquals("12345", result.getVariableSymbol());
        assertNotNull(result.getQrCode());
        assertFalse(result.getQrCode().isEmpty());
    }

    @Test
    void getPaymentInfo_ShouldHandleNullOrderIdentifier() {
        OrderDto order = new OrderDto();
        order.setOrderIdentifier(null);
        order.setFinalPrice(new BigDecimal("100.50"));

        PaymentDto payment = new PaymentDto();
        payment.setType(PaymentType.BANK_TRANSFER);

        PaymentInfoDto result = service.getPaymentInfo(order, payment);

        assertNotNull(result);
        assertNull(result.getVariableSymbol());
        assertNotNull(result.getQrCode());
    }
}
