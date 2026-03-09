package sk.tany.rest.api.domain.supplier;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sk.tany.rest.api.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierInvoice extends BaseEntity {
    private String supplierName;
    private BigDecimal priceWithVat;
    private BigDecimal priceWithoutVat;
    private BigDecimal vatValue;
    private Instant dateCreated;
    private String supplierVatIdentifier;
    private Instant taxDate;
    private String invoiceNumber;
    private String paymentReference;

    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "supplierName": return supplierName;
            case "priceWithVat": return priceWithVat;
            case "priceWithoutVat": return priceWithoutVat;
            case "vatValue": return vatValue;
            case "dateCreated": return dateCreated;
            case "supplierVatIdentifier": return supplierVatIdentifier;
            case "taxDate": return taxDate;
            case "invoiceNumber": return invoiceNumber;
            case "paymentReference": return paymentReference;
            default: return super.getSortValue(field);
        }
    }
}
