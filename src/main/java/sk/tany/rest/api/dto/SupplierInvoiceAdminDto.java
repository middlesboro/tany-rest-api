package sk.tany.rest.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class SupplierInvoiceAdminDto {
    private String id;
    private Instant createDate;
    private Instant updateDate;
    private String supplierName;
    private BigDecimal priceWithVat;
    private BigDecimal priceWithoutVat;
    private BigDecimal vatValue;
    private LocalDate dateCreated;
    private String supplierVatIdentifier;
    private LocalDate taxDate;
    private String invoiceNumber;
    private String paymentReference;
}
