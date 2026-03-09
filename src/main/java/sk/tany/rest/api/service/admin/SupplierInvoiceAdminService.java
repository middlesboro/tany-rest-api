package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.SupplierInvoiceAdminDto;

import java.time.Instant;
import java.time.LocalDate;

public interface SupplierInvoiceAdminService {
    Page<SupplierInvoiceAdminDto> list(Pageable pageable, String query, LocalDate createDateFrom, LocalDate createDateTo);
    SupplierInvoiceAdminDto get(String id);
    SupplierInvoiceAdminDto create(SupplierInvoiceAdminDto dto);
    SupplierInvoiceAdminDto update(String id, SupplierInvoiceAdminDto dto);
    void delete(String id);
    byte[] exportCsv(Instant createDateFrom, Instant createDateTo);
}
