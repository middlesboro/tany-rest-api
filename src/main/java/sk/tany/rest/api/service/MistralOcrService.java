package sk.tany.rest.api.service;

import sk.tany.rest.api.domain.supplier.SupplierInvoice;

public interface MistralOcrService {
    SupplierInvoice extractInvoiceData(byte[] pdfBytes, String fileName);
}
