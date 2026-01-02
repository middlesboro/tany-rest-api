package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.admin.PaymentAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentAdminService paymentService;
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody PaymentDto payment) {
        PaymentDto savedPayment = paymentService.save(payment);
        return new ResponseEntity<>(savedPayment, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<PaymentDto> getPayments(Pageable pageable) {
        return paymentService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable String id) {
        return paymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDto> updatePayment(@PathVariable String id, @RequestBody PaymentDto payment) {
        PaymentDto updatedPayment = paymentService.update(id, payment);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<PaymentDto> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return paymentService.findById(id)
                .map(payment -> {
                    String imageUrl = imageService.upload(file);
                    payment.setImage(imageUrl);
                    PaymentDto updatedPayment = paymentService.update(id, payment);
                    return ResponseEntity.ok(updatedPayment);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
