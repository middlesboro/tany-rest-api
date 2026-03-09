package sk.tany.rest.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class SupplierInvoiceAdminDto {
    private String id;
    private Instant createDate;
    private Instant updateDate;

    private String supplierName;
    private BigDecimal priceWithVat;
    private BigDecimal priceWithoutVat;
    private BigDecimal vatValue;
    private Instant dateCreated;
    private String supplierVatIdentifier;
    private Instant taxDate;
    private String invoiceNumber;
    private String paymentReference;
}
