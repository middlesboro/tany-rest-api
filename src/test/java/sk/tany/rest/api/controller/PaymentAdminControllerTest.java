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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.controller.admin.PaymentAdminController;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.service.admin.PaymentAdminService;
import sk.tany.rest.api.service.common.ImageService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaymentAdminControllerTest {

    @Mock
    private PaymentAdminService paymentService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private PaymentAdminController paymentAdminController;

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

        Page<PaymentDto> result = paymentAdminController.getPayments(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Payment", result.getContent().getFirst().getName());
        verify(paymentService, times(1)).findAll(pageable);
    }

    @Test
    void createPayment_ShouldReturnCreatedPayment() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setName("New Payment");
        when(paymentService.save(paymentDto)).thenReturn(paymentDto);

        ResponseEntity<PaymentDto> result = paymentAdminController.createPayment(paymentDto);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(paymentDto, result.getBody());
    }

    @Test
    void getPayment_ShouldReturnPayment_WhenFound() {
        String id = "1";
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(id);
        when(paymentService.findById(id)).thenReturn(Optional.of(paymentDto));

        ResponseEntity<PaymentDto> result = paymentAdminController.getPayment(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(paymentDto, result.getBody());
    }

    @Test
    void updatePayment_ShouldReturnUpdatedPayment() {
        String id = "1";
        PaymentDto paymentDto = new PaymentDto();
        when(paymentService.update(id, paymentDto)).thenReturn(paymentDto);

        ResponseEntity<PaymentDto> result = paymentAdminController.updatePayment(id, paymentDto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(paymentDto, result.getBody());
    }

    @Test
    void deletePayment_ShouldReturnNoContent() {
        String id = "1";
        ResponseEntity<Void> result = paymentAdminController.deletePayment(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(paymentService, times(1)).deleteById(id);
    }

    @Test
    void uploadImage_ShouldReturnUpdatedPayment() {
        String id = "1";
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(id);
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        String imageUrl = "http://image.url";

        when(paymentService.findById(id)).thenReturn(Optional.of(paymentDto));
        when(imageService.upload(eq(file), isNull())).thenReturn(imageUrl);
        when(paymentService.update(eq(id), any(PaymentDto.class))).thenReturn(paymentDto);

        ResponseEntity<PaymentDto> result = paymentAdminController.uploadImage(id, file);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(imageService, times(1)).upload(file, null);
    }
}
