package sk.tany.features.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tany.features.dto.InvoiceDataDto;
import sk.tany.features.service.admin.InvoiceService;

@Slf4j
@RestController
@RequestMapping("/api/features/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateInvoice(@RequestBody InvoiceDataDto request) {
        log.info("Received request to generate invoice for order {}", request.getOrderIdentifier());
        byte[] pdfBytes = invoiceService.generateInvoice(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice_" + request.getOrderIdentifier() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/credit-note")
    public ResponseEntity<byte[]> generateCreditNote(@RequestBody InvoiceDataDto request) {
        log.info("Received request to generate credit note for order {}", request.getOrderIdentifier());
        byte[] pdfBytes = invoiceService.generateCreditNote(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"credit_note_" + request.getOrderIdentifier() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
