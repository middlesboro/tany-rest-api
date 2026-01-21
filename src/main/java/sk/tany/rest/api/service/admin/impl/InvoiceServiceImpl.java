package sk.tany.rest.api.service.admin.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.service.admin.InvoiceService;

import java.awt.Color;
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

    private static final Color BRAND_COLOR = new Color(44, 62, 80);
    private static final Color ACCENT_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

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
                            .collect(Collectors.toList())
            ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        } else {
            productMap = Map.of();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36); // Margins
            PdfWriter.getInstance(document, baos);
            document.open();

            addContent(document, order, customer, carrierName, paymentName, productMap);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    private void addContent(Document document, Order order, Customer customer, String carrierName, String paymentName, Map<String, Product> productMap) throws DocumentException {
        // --- Header Section ---
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Paragraph("FAKTÚRA", getSlovakFont(24, Font.BOLD, TEXT_WHITE)));
        titleCell.setBackgroundColor(BRAND_COLOR);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingTop(20);
        titleCell.setPaddingBottom(10);
        titleCell.setPaddingLeft(20);
        headerTable.addCell(titleCell);

        PdfPCell numberCell = new PdfPCell(new Paragraph("Číslo dokladu: 2026/" + order.getOrderIdentifier(), getSlovakFont(12, Font.NORMAL, TEXT_WHITE)));
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

        // Supplier
        PdfPCell supplierCell = new PdfPCell();
        supplierCell.setBorder(Rectangle.NO_BORDER);
        supplierCell.addElement(new Paragraph("DODÁVATEĽ", getSlovakFont(10, Font.BOLD, Color.GRAY)));
        supplierCell.addElement(new Paragraph("Bc. Tatiana Grňová - Tany.sk", getSlovakFont(12, Font.BOLD)));
        supplierCell.addElement(new Paragraph("Budatínska 3916/24", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("85106 Bratislava", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("Slovenská republika", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("\nIČO: 50 350 595", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("DIČ: 1077433060", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("IČ DPH: SK1077433060", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("\nEmail: info@tany.sk", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("Tel: +421 944 432 457", getSlovakFont(10, Font.NORMAL)));

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
        customerCell.addElement(new Paragraph("Slovenská republika", getSlovakFont(10, Font.NORMAL)));

        infoTable.addCell(customerCell);
        document.add(infoTable);

        // --- Dates & Details ---
        PdfPTable detailsTable = new PdfPTable(3);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingBefore(20);
        detailsTable.setWidths(new float[]{1, 1, 1});

        addDetailCell(detailsTable, "Dátum vystavenia", formatDate(order.getCreateDate()));
        addDetailCell(detailsTable, "Dátum dodania", formatDate(order.getCreateDate()));
        addDetailCell(detailsTable, "Dátum splatnosti", formatDate(order.getCreateDate())); // Logic for due date?

        document.add(detailsTable);

        // Payment Info Block
        PdfPTable paymentInfoTable = new PdfPTable(2);
        paymentInfoTable.setWidthPercentage(100);
        paymentInfoTable.setSpacingBefore(10);
        paymentInfoTable.setWidths(new float[]{1.5f, 1});

        PdfPCell bankCell = new PdfPCell();
        bankCell.setBorder(Rectangle.NO_BORDER);
        bankCell.addElement(new Paragraph("BANKOVÉ SPOJENIE", getSlovakFont(9, Font.BOLD, Color.GRAY)));
        bankCell.addElement(new Paragraph("mBank / BREXSKBX", getSlovakFont(10, Font.NORMAL)));
        bankCell.addElement(new Paragraph("IBAN: SK52 8360 5207 0042 0571 4953", getSlovakFont(10, Font.BOLD)));
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
            for (PriceItem item : order.getPriceBreakDown().getItems()) {
                String name = item.getName();
                String code = "";

                if (item.getType() == PriceItemType.PRODUCT) {
                    Product product = productMap.get(item.getId());
                    if (product != null) {
                        code = product.getProductCode();
                    }
                }

                BigDecimal lineTotalWithVat = item.getPriceWithVat();
                BigDecimal lineTotalBase = item.getPriceWithoutVat();
                BigDecimal lineTotalVat = item.getVatValue();

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

        BigDecimal totalBase = order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPriceWithoutVat() : BigDecimal.ZERO;
        BigDecimal totalVat = order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPriceVatValue() : BigDecimal.ZERO;
        BigDecimal totalWithVat = order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPrice() : BigDecimal.ZERO;

        // Empty cell for left spacing (or notes)
        PdfPCell leftSpace = new PdfPCell(new Paragraph(""));
        leftSpace.setBorder(Rectangle.NO_BORDER);
        footerTable.addCell(leftSpace);

        // Right Side (Totals)
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);

        totalTable.addCell(createLabelCell("Suma bez DPH:"));
        totalTable.addCell(createRightAlignedCell(totalBase.toString() + " €", 10, false));

        totalTable.addCell(createLabelCell("DPH (20%):"));
        totalTable.addCell(createRightAlignedCell(totalVat.toString() + " €", 10, false));

        PdfPCell totalLabel = new PdfPCell(new Paragraph("K ÚHRADE", getSlovakFont(12, Font.BOLD, BRAND_COLOR)));
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

        // Footer Note
        Paragraph footerNote = new Paragraph("\nĎakujeme za Vašu objednávku.", getSlovakFont(10, Font.ITALIC, Color.GRAY));
        footerNote.setAlignment(Element.ALIGN_CENTER);
        footerNote.setSpacingBefore(30);
        document.add(footerNote);
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
}
