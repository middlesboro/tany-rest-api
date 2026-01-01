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
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.service.PaymentService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

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

        Page<PaymentDto> result = paymentController.getPayments(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Payment", result.getContent().get(0).getName());
        verify(paymentService, times(1)).findAll(pageable);
    }
}
