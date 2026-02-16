package sk.tany.rest.api.service.admin.impl;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.exception.InvoiceException;
import sk.tany.rest.api.exception.OrderException;
import sk.tany.rest.api.service.admin.InvoiceService;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final OrderRepository orderRepository;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ShopSettingsRepository shopSettingsRepository;

    private static final Color BRAND_COLOR = new Color(44, 62, 80);
    private static final Color ACCENT_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_WHITE = Color.WHITE;

    // Attempt to use CP1250 for Slovak support if possible, otherwise fallback to standard
    private Font getSlovakFont(int size, int style) {
        return getSlovakFont(size, style, Color.BLACK);
    }

    private Font getSlovakFont(int size, int style, Color color) {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, style, color);
        } catch (Exception e) {
            return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
        }
    }

    @Override
    public byte[] generateInvoice(String orderId) {
        return generateDocument(orderId, false);
    }

    @Override
    public byte[] generateCreditNote(String orderId) {
        return generateDocument(orderId, true);
    }

    private byte[] generateDocument(String orderId, boolean isCreditNote) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException.NotFound("Order not found: " + orderId));

        Customer customer = null;
        if (order.getCustomerId() != null) {
            customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        }

        String carrierName = carrierRepository.findById(order.getCarrierId())
                .map(Carrier::getName)
                .orElse("Unknown Carrier");

        String paymentName = paymentRepository.findById(order.getPaymentId())
                .map(Payment::getName)
                .orElse("Unknown Payment");

        // Fetch products for codes
        Map<String, Product> productMap;
        if (order.getPriceBreakDown() != null && order.getPriceBreakDown().getItems() != null) {
            productMap = productRepository.findAllById(
                    order.getPriceBreakDown().getItems().stream()
                            .filter(i -> i.getType() == PriceItemType.PRODUCT)
                            .map(PriceItem::getId)
                            .toList()
            ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        } else {
            productMap = Map.of();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36); // Margins
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new InvoiceFooter());
            document.open();

            addContent(document, order, customer, carrierName, paymentName, productMap, isCreditNote);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new InvoiceException("Failed to generate invoice", e);
        }
    }

    private void addContent(Document document, Order order, Customer customer, String carrierName, String paymentName, Map<String, Product> productMap, boolean isCreditNote) throws DocumentException {
        String title = isCreditNote ? "DOBROPIS" : "FAKTÚRA";

        int year = 2026;
        if (isCreditNote && order.getCancelDate() != null) {
            year = java.time.LocalDateTime.ofInstant(order.getCancelDate(), ZoneId.systemDefault()).getYear();
        } else if (order.getCreateDate() != null) {
            year = java.time.LocalDateTime.ofInstant(order.getCreateDate(), ZoneId.systemDefault()).getYear();
        }
        String docNumber = String.format("%d%06d", year, isCreditNote ? order.getCreditNoteIdentifier() : order.getOrderIdentifier());

        // --- Header Section ---
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Paragraph(title, getSlovakFont(24, Font.BOLD, TEXT_WHITE)));
        titleCell.setBackgroundColor(BRAND_COLOR);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingTop(20);
        titleCell.setPaddingBottom(10);
        titleCell.setPaddingLeft(20);
        headerTable.addCell(titleCell);

        PdfPCell numberCell = new PdfPCell(new Paragraph("Číslo dokladu: " + docNumber, getSlovakFont(12, Font.NORMAL, TEXT_WHITE)));
        numberCell.setBackgroundColor(BRAND_COLOR);
        numberCell.setBorder(Rectangle.NO_BORDER);
        numberCell.setPaddingBottom(20);
        numberCell.setPaddingLeft(20);
        headerTable.addCell(numberCell);

        document.add(headerTable);

        // --- Supplier & Customer Section ---
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(20);
        infoTable.setWidths(new float[]{1, 1});

        ShopSettings shopSettings = getShopSettings();

        // Supplier
        PdfPCell supplierCell = new PdfPCell();
        supplierCell.setBorder(Rectangle.NO_BORDER);
        supplierCell.addElement(new Paragraph("DODÁVATEĽ", getSlovakFont(10, Font.BOLD, Color.GRAY)));
        supplierCell.addElement(new Paragraph(shopSettings.getOrganizationName(), getSlovakFont(12, Font.BOLD)));
        supplierCell.addElement(new Paragraph(shopSettings.getShopStreet(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph(shopSettings.getShopZip() + " " + shopSettings.getShopCity(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph(shopSettings.getDefaultCountry(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("\nIČO: " + shopSettings.getIco(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("DIČ: " + shopSettings.getDic(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("IČ DPH: " + shopSettings.getVatNumber(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("\nEmail: " + shopSettings.getShopEmail(), getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("Tel: " + shopSettings.getShopPhoneNumber(), getSlovakFont(10, Font.NORMAL)));

        infoTable.addCell(supplierCell);

        // Customer
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBorder(Rectangle.NO_BORDER);
        customerCell.addElement(new Paragraph("ODBERATEĽ", getSlovakFont(10, Font.BOLD, Color.GRAY)));

        String customerName = "Neregistrovaný zákazník";
        if (customer != null && customer.getFirstname() != null) {
            customerName = customer.getFirstname() + " " + (customer.getLastname() != null ? customer.getLastname() : "");
        }

        String street = "";
        String city = "";
        String zip = "";
        if (order.getInvoiceAddress() != null) {
            street = order.getInvoiceAddress().getStreet();
            city = order.getInvoiceAddress().getCity();
            zip = order.getInvoiceAddress().getZip();
        }

        customerCell.addElement(new Paragraph(customerName, getSlovakFont(12, Font.BOLD)));
        customerCell.addElement(new Paragraph(street, getSlovakFont(10, Font.NORMAL)));
        customerCell.addElement(new Paragraph(zip + " " + city, getSlovakFont(10, Font.NORMAL)));
        customerCell.addElement(new Paragraph(shopSettings.getDefaultCountry(), getSlovakFont(10, Font.NORMAL)));

        infoTable.addCell(customerCell);
        document.add(infoTable);

        // --- Dates & Details ---
        PdfPTable detailsTable = new PdfPTable(3);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingBefore(20);
        detailsTable.setWidths(new float[]{1, 1, 1});

        java.time.Instant dateToUse = isCreditNote && order.getCancelDate() != null ? order.getCancelDate() : order.getCreateDate();
        addDetailCell(detailsTable, "Dátum vystavenia", formatDate(dateToUse));
        addDetailCell(detailsTable, "Dátum dodania", formatDate(dateToUse));
        addDetailCell(detailsTable, "Dátum splatnosti", formatDate(dateToUse)); // Logic for due date?

        document.add(detailsTable);

        // Payment Info Block
        PdfPTable paymentInfoTable = new PdfPTable(2);
        paymentInfoTable.setWidthPercentage(100);
        paymentInfoTable.setSpacingBefore(10);
        paymentInfoTable.setWidths(new float[]{1.5f, 1});

        PdfPCell bankCell = new PdfPCell();
        bankCell.setBorder(Rectangle.NO_BORDER);
        bankCell.addElement(new Paragraph("BANKOVÉ SPOJENIE", getSlovakFont(9, Font.BOLD, Color.GRAY)));
        bankCell.addElement(new Paragraph(shopSettings.getBankName() + " / " + shopSettings.getBankBic(), getSlovakFont(10, Font.NORMAL)));
        bankCell.addElement(new Paragraph("IBAN: " + shopSettings.getBankAccount(), getSlovakFont(10, Font.BOLD)));
        paymentInfoTable.addCell(bankCell);

        PdfPCell varSymbolCell = new PdfPCell();
        varSymbolCell.setBorder(Rectangle.NO_BORDER);
        varSymbolCell.addElement(new Paragraph("VARIABILNÝ SYMBOL", getSlovakFont(9, Font.BOLD, Color.GRAY)));
        varSymbolCell.addElement(new Paragraph(String.valueOf(order.getOrderIdentifier()), getSlovakFont(12, Font.BOLD)));
        paymentInfoTable.addCell(varSymbolCell);

        document.add(paymentInfoTable);

        // --- Items Table ---
        PdfPTable itemsTable = new PdfPTable(7);
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingBefore(30);
        itemsTable.setWidths(new float[]{4, 1.5f, 1, 1.5f, 1.5f, 1, 1.5f});
        itemsTable.setHeaderRows(1);

        String[] headers = {"POPIS", "CENA", "DPH %", "DPH", "CENA S DPH", "KS", "SPOLU"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, getSlovakFont(8, Font.BOLD, BRAND_COLOR)));
            cell.setBackgroundColor(ACCENT_COLOR);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BRAND_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        if (order.getPriceBreakDown() != null && order.getPriceBreakDown().getItems() != null) {
            boolean alternate = false;
            BigDecimal globalMultiplier = isCreditNote ? new BigDecimal("-1") : BigDecimal.ONE;

            for (PriceItem item : order.getPriceBreakDown().getItems()) {
                String name = item.getName();
                String code = "";

                if (item.getType() == PriceItemType.PRODUCT) {
                    Product product = productMap.get(item.getId());
                    if (product != null) {
                        code = product.getProductCode();
                    }
                }

                BigDecimal itemMultiplier = globalMultiplier;
                if (isCreditNote && item.getType() == PriceItemType.DISCOUNT) {
                    itemMultiplier = BigDecimal.ONE;
                }

                BigDecimal lineTotalWithVat = item.getPriceWithVat().multiply(itemMultiplier);
                BigDecimal lineTotalBase = item.getPriceWithoutVat().multiply(itemMultiplier);
                BigDecimal lineTotalVat = item.getVatValue().multiply(itemMultiplier);

                BigDecimal qty = item.getQuantity() != null && item.getQuantity() > 0 ? new BigDecimal(item.getQuantity()) : BigDecimal.ONE;
                BigDecimal unitPriceWithVat = lineTotalWithVat.divide(qty, 2, RoundingMode.HALF_UP);
                BigDecimal unitPriceBase = lineTotalBase.divide(qty, 2, RoundingMode.HALF_UP);

                BigDecimal vatRate = BigDecimal.ZERO;
                if (lineTotalBase.compareTo(BigDecimal.ZERO) != 0) {
                    vatRate = lineTotalVat.divide(lineTotalBase, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                }

                Color rowColor = alternate ? new Color(250, 250, 250) : Color.WHITE;

                // Name & Code
                PdfPCell nameCell = new PdfPCell();
                nameCell.setBorder(Rectangle.BOTTOM);
                nameCell.setBorderColor(new Color(230, 230, 230));
                nameCell.setBackgroundColor(rowColor);
                nameCell.setPadding(8);
                nameCell.addElement(new Paragraph(name, getSlovakFont(9, Font.NORMAL)));
                if (!code.isEmpty()) {
                    nameCell.addElement(new Paragraph(code, getSlovakFont(8, Font.NORMAL, Color.GRAY)));
                }
                itemsTable.addCell(nameCell);

                itemsTable.addCell(createItemCell(unitPriceBase.toString() + " €", rowColor));
                itemsTable.addCell(createItemCell(vatRate.intValue() + "%", rowColor));
                itemsTable.addCell(createItemCell(lineTotalVat.toString() + " €", rowColor));
                itemsTable.addCell(createItemCell(unitPriceWithVat.toString() + " €", rowColor));
                itemsTable.addCell(createItemCell(String.valueOf(item.getQuantity()), rowColor));
                itemsTable.addCell(createItemCell(lineTotalWithVat.toString() + " €", rowColor, Font.BOLD));

                alternate = !alternate;
            }
        }

        document.add(itemsTable);

        // --- Footer / Totals ---
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(20);
        footerTable.setWidths(new float[]{1.5f, 1});

        BigDecimal multiplier = isCreditNote ? new BigDecimal("-1") : BigDecimal.ONE;
        BigDecimal totalBase = (order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPriceWithoutVat() : BigDecimal.ZERO).multiply(multiplier);
        BigDecimal totalVat = (order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPriceVatValue() : BigDecimal.ZERO).multiply(multiplier);
        BigDecimal totalWithVat = (order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPrice() : BigDecimal.ZERO).multiply(multiplier);

        // Empty cell for left spacing (or notes)
        PdfPCell leftSpace = new PdfPCell(new Paragraph(""));
        leftSpace.setBorder(Rectangle.NO_BORDER);
        footerTable.addCell(leftSpace);

        // Right Side (Totals)
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);

        totalTable.addCell(createLabelCell("Suma bez DPH:"));
        totalTable.addCell(createRightAlignedCell(totalBase.toString() + " €", 10, false));

        totalTable.addCell(createLabelCell("DPH (23%):"));
        totalTable.addCell(createRightAlignedCell(totalVat.toString() + " €", 10, false));

        PdfPCell totalLabel = new PdfPCell(new Paragraph("Spolu", getSlovakFont(12, Font.BOLD, BRAND_COLOR)));
        totalLabel.setBorder(Rectangle.TOP);
        totalLabel.setBorderColor(BRAND_COLOR);
        totalLabel.setPaddingTop(10);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Paragraph(totalWithVat.toString() + " €", getSlovakFont(14, Font.BOLD, BRAND_COLOR)));
        totalValue.setBorder(Rectangle.TOP);
        totalValue.setBorderColor(BRAND_COLOR);
        totalValue.setPaddingTop(10);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalValue);

        PdfPCell totalWrapper = new PdfPCell(totalTable);
        totalWrapper.setBorder(Rectangle.NO_BORDER);
        footerTable.addCell(totalWrapper);

        document.add(footerTable);

        if (!isCreditNote) {
            // Footer Note
            Paragraph footerNote = new Paragraph("\nĎakujeme za Vašu objednávku.", getSlovakFont(10, Font.ITALIC, Color.GRAY));
            footerNote.setAlignment(Element.ALIGN_CENTER);
            footerNote.setSpacingBefore(30);
            document.add(footerNote);
        }

        if (!isCreditNote && order.getStatus() == sk.tany.rest.api.domain.order.OrderStatus.PAID) {
            Paragraph paidNote = new Paragraph("FAKTÚRA JE UŽ UHRADENÁ", getSlovakFont(12, Font.BOLD, BRAND_COLOR));
            paidNote.setAlignment(Element.ALIGN_CENTER);
            paidNote.setSpacingBefore(10);
            document.add(paidNote);
        }

        // Add Signature
        try {
            Image signature = Image.getInstance(getClass().getResource("/podpis_fa.png"));
            signature.scaleToFit(120, 60);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(20);
            document.add(signature);
        } catch (Exception e) {
            // Log or ignore if signature is missing
            System.err.println("Failed to load signature: " + e.getMessage());
        }
    }

    private class InvoiceFooter extends PdfPageEventHelper {
        ShopSettings shopSettings = getShopSettings();

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String footerText = "Tany.sk | Email: " + shopSettings.getShopEmail() + " | Tel: " + shopSettings.getShopPhoneNumber();
            Phrase footer = new Phrase(footerText, getSlovakFont(9, Font.NORMAL, Color.GRAY));
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }

    private String formatDate(java.time.Instant date) {
        if (date == null) return "";
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault()).format(date);
    }

    private void addDetailCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph(label, getSlovakFont(9, Font.BOLD, Color.GRAY)));
        cell.addElement(new Paragraph(value, getSlovakFont(10, Font.NORMAL)));
        table.addCell(cell);
    }

    private PdfPCell createItemCell(String text, Color bgColor) {
        return createItemCell(text, bgColor, Font.NORMAL);
    }

    private PdfPCell createItemCell(String text, Color bgColor, int style) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(9, style)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(230, 230, 230));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(10, Font.NORMAL, Color.GRAY)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(5);
        return cell;
    }

    private PdfPCell createRightAlignedCell(String text, int size, boolean bold) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(size, bold ? Font.BOLD : Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(5);
        return cell;
    }

    ShopSettings getShopSettings() {
        return shopSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(ShopSettings::new);
    }
}
