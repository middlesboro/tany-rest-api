package sk.tany.rest.api.service.scheduler;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeBodyPart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.config.EmailImapConfig;
import sk.tany.rest.api.domain.supplier.SupplierInvoice;
import sk.tany.rest.api.domain.supplier.SupplierInvoiceRepository;
import sk.tany.rest.api.service.MistralOcrService;
import sk.tany.rest.api.service.OneDriveService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEmailScheduler {

    private final EmailImapConfig imapConfig;
    private final OneDriveService oneDriveService;
    private final MistralOcrService mistralOcrService;
    private final SupplierInvoiceRepository supplierInvoiceRepository;

    @Scheduled(cron = "0 0 23 * * ?") // 11 PM daily
    public void processEmails() {
        if (imapConfig.getHost() == null || imapConfig.getHost().isBlank()) {
            log.info("IMAP configuration is missing, skipping InvoiceEmailScheduler");
            return;
        }

        log.info("Starting scheduled invoice email processing");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imapConfig.getHost());
        props.put("mail.imaps.port", imapConfig.getPort());
        props.put("mail.imaps.ssl.enable", "true");

        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword());

            Folder folder = store.getFolder(imapConfig.getFolder());
            if (!folder.exists()) {
                log.error("IMAP folder {} does not exist", imapConfig.getFolder());
                return;
            }

            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.getMessages();
            for (Message message : messages) {
                if (!message.isSet(Flags.Flag.SEEN)) {
                    processMessage(message);
                    message.setFlag(Flags.Flag.SEEN, true); // Mark as read
                }
            }

            folder.close(false);
            store.close();

            log.info("Finished scheduled invoice email processing");
        } catch (Exception e) {
            log.error("Error during scheduled invoice email processing", e);
        }
    }

    private void processMessage(Message message) {
        try {
            log.info("Processing email: {}", message.getSubject());
            if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    Part part = multipart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
                        String fileName = part.getFileName();
                        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                            byte[] pdfBytes = readPart(part);
                            processPdfAttachment(fileName, pdfBytes);
                        }
                    }
                }
            } else if (message.isMimeType("application/pdf")) {
                String fileName = message.getFileName() != null ? message.getFileName() : "attachment.pdf";
                byte[] pdfBytes = readPart(message);
                processPdfAttachment(fileName, pdfBytes);
            }
        } catch (Exception e) {
            log.error("Failed to process message", e);
        }
    }

    private byte[] readPart(Part part) throws Exception {
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    private void processPdfAttachment(String fileName, byte[] pdfBytes) {
        try {
            log.info("Processing PDF attachment: {}", fileName);

            // 1. Upload to OneDrive
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            String folderPath = String.format("/Tany/Faktury dodavatelia/%d/%02d", year, month);

            oneDriveService.uploadFile(folderPath, fileName, pdfBytes);
            log.info("Uploaded {} to OneDrive path {}", fileName, folderPath);

            // 2. OCR and Data Extraction
            SupplierInvoice extractedData = mistralOcrService.extractInvoiceData(pdfBytes, fileName);

            // 3. Save to database
            if (extractedData != null) {
                supplierInvoiceRepository.save(extractedData);
                log.info("Successfully extracted and saved invoice data for {}", fileName);
            } else {
                log.warn("Failed to extract invoice data for {}", fileName);
            }

        } catch (Exception e) {
            log.error("Error processing PDF attachment {}", fileName, e);
        }
    }
}
