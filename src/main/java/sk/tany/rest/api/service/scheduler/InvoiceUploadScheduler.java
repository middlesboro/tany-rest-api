package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.service.OneDriveService;
import sk.tany.rest.api.service.admin.InvoiceService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceUploadScheduler {

    @Value("${onedrive.send-documents}")
    private boolean sendDocumentsToOneDrive;

    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;
    private final OneDriveService oneDriveService;

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processUploads() {
        if (!sendDocumentsToOneDrive) {
            log.info("Skipping upload of send documents");
        }

        log.info("Starting scheduled invoice/credit note upload to OneDrive");
        uploadInvoices();
        uploadCreditNotes();
        log.info("Finished scheduled invoice/credit note upload to OneDrive");
    }

    private void uploadInvoices() {
        List<Order> orders = orderRepository.findByInvoiceUploadedToOneDriveFalse();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELED) {
                continue; // Skip canceled orders for invoices (they get credit notes)
            }
            try {
                byte[] pdf = invoiceService.generateInvoice(order.getId());
                LocalDateTime date = order.getCreateDate() != null ?
                        LocalDateTime.ofInstant(order.getCreateDate(), ZoneId.systemDefault()) :
                        LocalDateTime.now();
                int year = date.getYear();
                int month = date.getMonthValue();
                String folderPath = String.format("/Tany/Faktury zakaznici/%d/%02d", year, month);
                String fileName = String.format("invoice_%d%06d.pdf", year, order.getOrderIdentifier());

                oneDriveService.uploadFile(folderPath, fileName, pdf);

                order.setInvoiceUploadedToOneDrive(true);
                orderRepository.save(order);
                log.info("Uploaded invoice for order {}", order.getOrderIdentifier());
            } catch (Exception e) {
                log.error("Failed to upload invoice for order {}", order.getOrderIdentifier(), e);
            }
        }
    }

    private void uploadCreditNotes() {
        List<Order> orders = orderRepository.findByCreditNoteUploadedToOneDriveFalse();
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.CANCELED) {
                continue; // Only canceled orders have credit notes
            }
            try {
                byte[] pdf = invoiceService.generateCreditNote(order.getId());
                LocalDateTime date = order.getCancelDate() != null ?
                        LocalDateTime.ofInstant(order.getCancelDate(), ZoneId.systemDefault()) :
                        LocalDateTime.now();
                int year = date.getYear();
                int month = date.getMonthValue();
                String folderPath = String.format("/Tany/Dobropisy/%d/%d", year, month);
                String fileName = String.format("dobropis_%d%06d.pdf", year, order.getCreditNoteIdentifier());

                oneDriveService.uploadFile(folderPath, fileName, pdf);

                order.setCreditNoteUploadedToOneDrive(true);
                orderRepository.save(order);
                log.info("Uploaded credit note for order {}", order.getOrderIdentifier());
            } catch (Exception e) {
                log.error("Failed to upload credit note for order {}", order.getOrderIdentifier(), e);
            }
        }
    }
}
