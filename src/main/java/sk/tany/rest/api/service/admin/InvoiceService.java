package sk.tany.rest.api.service.admin;

public interface InvoiceService {
    byte[] generateInvoice(String orderId);
    byte[] generateCreditNote(String orderId);
}
