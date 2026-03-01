package sk.tany.features.service.admin;

import sk.tany.features.dto.InvoiceDataDto;

public interface InvoiceService {
    byte[] generateInvoice(InvoiceDataDto invoiceData);
    byte[] generateCreditNote(InvoiceDataDto invoiceData);
}
