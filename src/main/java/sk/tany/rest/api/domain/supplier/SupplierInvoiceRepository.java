package sk.tany.rest.api.domain.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface SupplierInvoiceRepository extends MongoRepository<SupplierInvoice, String> {
    Page<SupplierInvoice> findBySupplierNameContainingIgnoreCaseOrInvoiceNumberContainingIgnoreCaseOrSupplierVatIdentifierContainingIgnoreCase(
        String name, String invoiceNumber, String vatIdentifier, Pageable pageable
    );

    Page<SupplierInvoice> findByCreateDateBetween(Instant from, Instant to, Pageable pageable);

    List<SupplierInvoice> findByCreateDateBetween(LocalDate from, LocalDate to);
}
