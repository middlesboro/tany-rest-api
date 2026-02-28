package sk.tany.features.service.admin;

public interface InvoiceService {
    byte[] generateInvoice(String orderId);
    byte[] generateCreditNote(String orderId);
}
