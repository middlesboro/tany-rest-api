package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.SupplierInvoiceAdminDto;
import sk.tany.rest.api.service.admin.SupplierInvoiceAdminService;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/supplier-invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupplierInvoiceAdminController {

    private final SupplierInvoiceAdminService service;

    @GetMapping
    public ResponseEntity<Page<SupplierInvoiceAdminDto>> list(
            Pageable pageable,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createDateTo) {
        return ResponseEntity.ok(service.list(pageable, query, createDateFrom, createDateTo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierInvoiceAdminDto> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<SupplierInvoiceAdminDto> create(@RequestBody SupplierInvoiceAdminDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierInvoiceAdminDto> update(@PathVariable String id, @RequestBody SupplierInvoiceAdminDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createDateTo) {

        byte[] csvData = service.exportCsv(createDateFrom, createDateTo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "supplier-invoices.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
