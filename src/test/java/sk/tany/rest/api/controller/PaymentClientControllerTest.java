package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.controller.client.PaymentClientController;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.client.payment.PaymentOrderIdResponse;
import sk.tany.rest.api.service.client.PaymentClientService;
import sk.tany.rest.api.service.client.payment.impl.BesteronPaymentTypeService;
import sk.tany.rest.api.service.client.payment.impl.GlobalPaymentsPaymentTypeService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentClientControllerTest {

    @Mock
    private PaymentClientService paymentService;

    @Mock
    private GlobalPaymentsPaymentTypeService globalPaymentsPaymentService;

    @Mock
    private BesteronPaymentTypeService besteronPaymentTypeService;

    @InjectMocks
    private PaymentClientController paymentClientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPayments_ShouldReturnPagedPayments() {
        Pageable pageable = PageRequest.of(0, 10);
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setName("Test Payment");
        Page<PaymentDto> paymentPage = new PageImpl<>(Collections.singletonList(paymentDto));

        when(paymentService.findAll(pageable)).thenReturn(paymentPage);

        Page<PaymentDto> result = paymentClientController.getPayments(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Payment", result.getContent().get(0).getName());
        verify(paymentService, times(1)).findAll(pageable);
    }

    @Test
    void getOrderIdByTransactionId_ShouldReturnOrderId_WhenTransactionExists() {
        String transactionId = "testTransactionId";
        String orderId = "testOrderId";

        when(besteronPaymentTypeService.getOrderIdByTransactionId(transactionId)).thenReturn(Optional.of(orderId));

        PaymentOrderIdResponse result = paymentClientController.getOrderIdByTransactionId(transactionId);

        assertEquals(orderId, result.getOrderId());
        verify(besteronPaymentTypeService, times(1)).getOrderIdByTransactionId(transactionId);
    }

    @Test
    void getOrderIdByTransactionId_ShouldThrowException_WhenTransactionNotFound() {
        String transactionId = "nonExistentTransactionId";

        when(besteronPaymentTypeService.getOrderIdByTransactionId(transactionId)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> paymentClientController.getOrderIdByTransactionId(transactionId));
        verify(besteronPaymentTypeService, times(1)).getOrderIdByTransactionId(transactionId);
    }
}
