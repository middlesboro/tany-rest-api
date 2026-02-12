package sk.tany.rest.api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.HtmlUtils;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.service.admin.InvoiceService;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.EmailService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final ResourceLoader resourceLoader;
    private final ProductClientService productClientService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        if (order == null) return;

        OrderStatus status = order.getStatus();

        if (order.getStatusHistory() == null) return;

        // Get the latest history entry for the current status
        OrderStatusHistory historyEntry = order.getStatusHistory().stream()
                .filter(h -> h.getStatus() == status)
                .reduce((first, second) -> second)
                .orElse(null);

        if (historyEntry == null) return;

        boolean orderChanged = false;

        // Stock Restoration Logic
        if (status == OrderStatus.CANCELED && !Boolean.TRUE.equals(historyEntry.getStockRestored())) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getId() != null && item.getQuantity() != null) {
                        try {
                            // Negative quantity to increase stock (subtracted in updateProductStock)
                            productClientService.updateProductStock(item.getId(), -item.getQuantity());
                        } catch (Exception e) {
                            log.error("Failed to restore stock for product {} in order {}", item.getId(), order.getOrderIdentifier(), e);
                        }
                    }
                }
            }
            historyEntry.setStockRestored(true);
            orderChanged = true;
        }

        // Email Sending Logic
        if (!Boolean.TRUE.equals(historyEntry.getEmailSent())) {
            boolean emailSent = false;
            if (status == OrderStatus.CREATED || status == OrderStatus.COD) {
                sendOrderCreatedEmail(order);
                emailSent = true;
            } else if (status == OrderStatus.SENT) {
                sendOrderSentEmail(order);
                emailSent = true;
            } else if (status == OrderStatus.PAID) {
                sendOrderPaidEmail(order);
                emailSent = true;
            }

            if (emailSent) {
                historyEntry.setEmailSent(true);
                orderChanged = true;
            }
        }

        if (orderChanged) {
            orderRepository.save(order);
        }
    }

    private void sendOrderCreatedEmail(Order order) {
        try {
            String template = getEmailTemplate("templates/email/order_created.html");

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            template = template.replace("{{firstname}}", HtmlUtils.htmlEscape(firstname));
            template = template.replace("{{orderIdentifier}}", String.valueOf(order.getOrderIdentifier()));
            template = template.replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            String orderConfirmationLink = frontendUrl + "/order/confirmation/" + order.getId();
            template = template.replace("{{orderConfirmationLink}}", orderConfirmationLink);

            // Products
            StringBuilder productsHtml = new StringBuilder();
            BigDecimal carrierPriceVal = BigDecimal.ZERO;
            BigDecimal paymentPriceVal = BigDecimal.ZERO;

            if (order.getPriceBreakDown() != null && order.getPriceBreakDown().getItems() != null) {
                for (PriceItem item : order.getPriceBreakDown().getItems()) {
                    if (item.getType() == PriceItemType.CARRIER) {
                        carrierPriceVal = item.getPriceWithVat();
                        continue;
                    }
                    if (item.getType() == PriceItemType.PAYMENT) {
                        paymentPriceVal = item.getPriceWithVat();
                        continue;
                    }

                    // Show Products and Discounts in the table
                    productsHtml.append("<tr>");
                    productsHtml.append("<td>").append(HtmlUtils.htmlEscape(item.getName())).append("</td>");
                    productsHtml.append("<td>").append(item.getQuantity()).append("</td>");

                    BigDecimal qty = item.getQuantity() != null && item.getQuantity() > 0 ? new BigDecimal(item.getQuantity()) : BigDecimal.ONE;
                    BigDecimal unitPrice = item.getPriceWithVat().divide(qty, 2, RoundingMode.HALF_UP);

                    productsHtml.append("<td>").append(String.format("%.2f&nbsp;€", unitPrice)).append("</td>");
                    productsHtml.append("<td>").append(String.format("%.2f&nbsp;€", item.getPriceWithVat())).append("</td>");
                    productsHtml.append("</tr>");
                }
            }
            template = template.replace("{{products}}", productsHtml.toString());

            // Carrier and Payment
            Carrier carrier = null;
            if (order.getCarrierId() != null) {
                carrier = carrierRepository.findById(order.getCarrierId()).orElse(null);
            }
            Payment payment = null;
            if (order.getPaymentId() != null) {
                payment = paymentRepository.findById(order.getPaymentId()).orElse(null);
            }

            String carrierName = carrier != null && carrier.getName() != null ? carrier.getName() : "Unknown Carrier";
            String carrierPrice = String.format("%.2f&nbsp;€", carrierPriceVal);
            template = template.replace("{{carrierName}}", HtmlUtils.htmlEscape(carrierName));
            template = template.replace("{{carrierPrice}}", carrierPrice);

            String paymentName = payment != null && payment.getName() != null ? payment.getName() : "Unknown Payment";
            String paymentPrice = String.format("%.2f&nbsp;€", paymentPriceVal);
            template = template.replace("{{paymentName}}", HtmlUtils.htmlEscape(paymentName));
            template = template.replace("{{paymentPrice}}", paymentPrice);

            // Address
            StringBuilder addressHtml = new StringBuilder();
            if (order.getDeliveryAddress() != null) {
                addressHtml.append("<p>").append(HtmlUtils.htmlEscape(order.getDeliveryAddress().getStreet())).append("</p>");
                addressHtml.append("<p>").append(HtmlUtils.htmlEscape(order.getDeliveryAddress().getZip())).append(" ")
                        .append(HtmlUtils.htmlEscape(order.getDeliveryAddress().getCity())).append("</p>");
            } else {
                addressHtml.append("<p>No delivery address provided</p>");
            }
            template = template.replace("{{deliveryAddress}}", addressHtml.toString());

            // Final Price
            BigDecimal finalPrice = order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPrice() : BigDecimal.ZERO;
            template = template.replace("{{finalPrice}}", String.format("%.2f&nbsp;€", finalPrice));

            byte[] invoiceBytes = invoiceService.generateInvoice(order.getId());
            File invoiceFile = File.createTempFile("faktura_" + order.getOrderIdentifier(), ".pdf");
            File odstupenieFile = createTempFileFromResource("classpath:formular-na-odstupenie-od-zmluvy-tany.sk.pdf", "odstupenie", ".pdf");
            File podmienkyFile = createTempFileFromResource("classpath:obchodne-podmienky.pdf", "podmienky", ".pdf");

            try {
                Files.write(invoiceFile.toPath(), invoiceBytes);
                emailService.sendEmail(order.getEmail(), "Objednávka č. " + order.getOrderIdentifier(), template, true, invoiceFile, odstupenieFile, podmienkyFile);
                log.info("Sent 'Order Created' email for order {}", order.getOrderIdentifier());
            } finally {
                // Cleanup temp files
                if (invoiceFile.exists()) {
                    invoiceFile.delete();
                }
                if (odstupenieFile != null && odstupenieFile.exists()) {
                    odstupenieFile.delete();
                }
                if (podmienkyFile != null && podmienkyFile.exists()) {
                    podmienkyFile.delete();
                }
            }

        } catch (IOException e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);
        }
    }

    private File createTempFileFromResource(String resourcePath, String prefix, String suffix) {
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (!resource.exists()) {
                log.warn("Resource not found: {}", resourcePath);
                return null;
            }
            File tempFile = File.createTempFile(prefix, suffix);
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        } catch (IOException e) {
            log.error("Failed to create temp file for resource: {}", resourcePath, e);
            return null;
        }
    }

    private void sendOrderSentEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send 'Order Sent' email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }
        try {
            String template = getEmailTemplate("templates/email/order_sent.html");

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String carrierLink = order.getCarrierOrderStateLink() != null ? order.getCarrierOrderStateLink() : "#";

            String body = template
                    .replace("{{firstname}}", firstname)
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{carrierOrderStateLink}}", carrierLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            emailService.sendEmail(order.getEmail(), "Objednávka odoslaná", body, true, null);
            log.info("Sent 'Order Sent' email for order {}", order.getOrderIdentifier());

        } catch (Exception e) {
            log.error("Failed to send 'Order Sent' email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private void sendOrderPaidEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send 'Order Paid' email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }
        try {
            String template = getEmailTemplate("templates/email/order_paid.html");

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String orderConfirmationLink = frontendUrl + "/order/confirmation/" + order.getId();

            String body = template
                    .replace("{{firstname}}", firstname)
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{orderConfirmationLink}}", orderConfirmationLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            emailService.sendEmail(order.getEmail(), "Objednávka zaplatená", body, true, null);
            log.info("Sent 'Order Paid' email for order {}", order.getOrderIdentifier());

        } catch (Exception e) {
            log.error("Failed to send 'Order Paid' email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private String getEmailTemplate(String template) throws java.io.IOException {
        ClassPathResource resource = new ClassPathResource(template);
        byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(data, StandardCharsets.UTF_8);
    }
}
