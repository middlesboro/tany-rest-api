package sk.tany.rest.api.service.admin.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.service.admin.*;

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

    private final OrderAdminService orderAdminService;
    private final CarrierAdminService carrierAdminService;
    private final PaymentAdminService paymentAdminService;
    private final ProductAdminService productAdminService;
    private final CustomerAdminService customerAdminService;

    private static final Font FONT_BOLD_12 = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static final Font FONT_NORMAL_10 = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

    // Attempt to use CP1250 for Slovak support if possible, otherwise fallback to standard
    private Font getSlovakFont(int size, int style) {
        try {
             BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
             return new Font(bf, size, style);
        } catch (Exception e) {
            return FontFactory.getFont(FontFactory.HELVETICA, size, style);
        }
    }

    @Override
    public byte[] generateInvoice(String orderId) {
        OrderDto order = orderAdminService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        CustomerDto customer = null;
        if (order.getCustomerId() != null) {
            customer = customerAdminService.findById(order.getCustomerId()).orElse(null);
        }

        String carrierName = carrierAdminService.findById(order.getCarrierId())
                .map(CarrierDto::getName)
                .orElse("Unknown Carrier");

        String paymentName = paymentAdminService.findById(order.getPaymentId())
                .map(PaymentDto::getName)
                .orElse("Unknown Payment");

        // Fetch products for codes
        Map<String, ProductDto> productMap = productAdminService.findAllByIds(
                order.getItems().stream().map(OrderItemDto::getId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(ProductDto::getId, p -> p));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addContent(document, order, customer, carrierName, paymentName, productMap);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    private void addContent(Document document, OrderDto order, CustomerDto customer, String carrierName, String paymentName, Map<String, ProductDto> productMap) throws DocumentException {
        // Main Table: 2 columns (Supplier, Customer/Invoice Info)
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1, 1});

        // Supplier Cell
        PdfPCell supplierCell = new PdfPCell();
        supplierCell.setBorder(Rectangle.BOX);
        supplierCell.setPadding(10);

        // This addElement call adds a Paragraph to the cell's composite elements list
        // which prevents further text mode operations if mixed incorrectly,
        // but for PdfPCell it's fine as long as we don't mix text mode and composite mode in a way that breaks.
        // However, OpenPDF/iText sometimes throws "Element not allowed" if we add things to a cell that expects only specific content or if structure is wrong.
        // In this case, Paragraph is allowed. The error might be from nested elements or specific constraints.

        supplierCell.addElement(new Paragraph("Dodávateľ:", getSlovakFont(12, Font.BOLD)));
        // Static Supplier Info (from image)
        supplierCell.addElement(new Paragraph("\nBc. Tatiana Grňová - Tany.sk", getSlovakFont(10, Font.BOLD)));
        supplierCell.addElement(new Paragraph("Budatínska 3916/24", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("85106 Bratislava", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("Slovenská republika", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("+421 944 432 457", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("info@tany.sk", getSlovakFont(10, Font.NORMAL)));

        supplierCell.addElement(new Paragraph("\nIČO:             50 350 595", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("DIČ:             1077433060", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("IČ DPH:          SK1077433060", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("\nČíslo živnostenského registra 470-18777", getSlovakFont(10, Font.NORMAL)));

        supplierCell.addElement(new Paragraph("-------------------------------------------------------"));

        supplierCell.addElement(new Paragraph("Názov banky:     mBank", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("SWIFT:           BREXSKBX", getSlovakFont(10, Font.NORMAL)));
        supplierCell.addElement(new Paragraph("IBAN:            SK52 8360 5207 0042 0571 4953", getSlovakFont(10, Font.NORMAL)));
        // Variable symbol usually Order Number
        supplierCell.addElement(new Paragraph("Variabilný symbol: " + order.getOrderIdentifier(), getSlovakFont(10, Font.BOLD))); // Simple cleanup for numeric

        mainTable.addCell(supplierCell);

        // Right Column Cell (Invoice Header + Customer)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setPadding(0);

        // Header
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell(new Paragraph("Daňový doklad č. 2026/" + order.getOrderIdentifier(), getSlovakFont(12, Font.BOLD)));
        headerCell.setBackgroundColor(new Color(230, 230, 230));
        headerCell.setPadding(10);
        headerCell.setBorder(Rectangle.BOTTOM);
        headerTable.addCell(headerCell);
        rightCell.addElement(headerTable);

        // Customer Info
        // Directly add paragraphs to rightCell instead of nesting PdfPCell
        Paragraph customerTitle = new Paragraph("Odberateľ:", getSlovakFont(12, Font.BOLD));
        customerTitle.setSpacingBefore(10);
        customerTitle.setIndentationLeft(10);
        rightCell.addElement(customerTitle);

        String customerName = "Unknown Customer";
        if (customer != null && customer.getFirstname() != null) {
            customerName = customer.getFirstname() + " " + (customer.getLastname() != null ? customer.getLastname() : "");
        }

        // Use delivery address or invoice address from order
        String street = "";
        String city = "";
        String zip = "";
        if (order.getInvoiceAddress() != null) {
            street = order.getInvoiceAddress().getStreet();
            city = order.getInvoiceAddress().getCity();
            zip = order.getInvoiceAddress().getZip();
        }

        Paragraph customerDetails = new Paragraph();
        customerDetails.setIndentationLeft(10);
        customerDetails.add(new Paragraph(customerName, getSlovakFont(10, Font.NORMAL)));
        customerDetails.add(new Paragraph(street, getSlovakFont(10, Font.NORMAL)));
        customerDetails.add(new Paragraph(zip + " " + city, getSlovakFont(10, Font.NORMAL)));
        customerDetails.add(new Paragraph("Slovenská republika", getSlovakFont(10, Font.NORMAL)));

        rightCell.addElement(customerDetails);

        // Dates Table
        PdfPTable datesTable = new PdfPTable(3);
        datesTable.setWidthPercentage(100);
        datesTable.setSpacingBefore(10);

        addDateCell(datesTable, "Dátum vystavenia", order.getCreateDate(), false);
        addDateCell(datesTable, "Dátum zdaniteľného\nplnenia", order.getCreateDate(), false);
        addDateCell(datesTable, "Dátum splatnosti", order.getCreateDate(), true); // Assume due on creation or add days

        rightCell.addElement(datesTable);

        // Order Extra Info
        Paragraph orderExtra = new Paragraph();
        orderExtra.setSpacingBefore(10);
        orderExtra.setIndentationLeft(10);
        orderExtra.add(new Chunk("Číslo objednávky:\t\t#" + order.getOrderIdentifier() + "\n", getSlovakFont(9, Font.NORMAL)));
        orderExtra.add(new Chunk("Spôsob platby:\t\t" + paymentName + "\n", getSlovakFont(9, Font.NORMAL)));
        orderExtra.add(new Chunk("Spôsob dopravy:\t\t" + carrierName + "\n", getSlovakFont(9, Font.NORMAL)));

        rightCell.addElement(orderExtra);

        mainTable.addCell(rightCell);
        document.add(mainTable);

        // Items Table
        PdfPTable itemsTable = new PdfPTable(7); // Prod, Base, VAT%, VAT val, Unit Price, Qty, Total
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingBefore(20);
        itemsTable.setWidths(new float[]{4, 1.5f, 1, 1.5f, 1.5f, 1, 1.5f});

        // Headers
        String[] headers = {"Produkt / Kód", "Základná\ncena\n/ Zľava", "Sadzba\nDPH", "DPH", "Jedn. cena\n(s DPH)", "Počet", "Celkom\n(s DPH)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, getSlovakFont(8, Font.BOLD)));
            cell.setBackgroundColor(new Color(230, 230, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        BigDecimal totalVat = BigDecimal.ZERO;
        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalWithVat = BigDecimal.ZERO;
        BigDecimal vatRate = new BigDecimal("0.20"); // 20%

        for (OrderItemDto item : order.getItems()) {
            ProductDto product = productMap.get(item.getId());
            String code = product != null ? product.getProductCode() : "";
            String ean = product != null ? product.getEan() : "";

            // Assuming Item Price is Final Price with VAT
            BigDecimal priceWithVat = item.getPrice();
            BigDecimal basePrice = priceWithVat.divide(BigDecimal.ONE.add(vatRate), 2, RoundingMode.HALF_UP);
            BigDecimal vatAmount = priceWithVat.subtract(basePrice);

            BigDecimal lineTotal = priceWithVat.multiply(new BigDecimal(item.getQuantity()));

            totalWithVat = totalWithVat.add(lineTotal);
            totalBase = totalBase.add(basePrice.multiply(new BigDecimal(item.getQuantity())));
            totalVat = totalVat.add(vatAmount.multiply(new BigDecimal(item.getQuantity())));

            // Product Cell
            PdfPCell prodCell = new PdfPCell();
            prodCell.addElement(new Paragraph(item.getName(), getSlovakFont(8, Font.NORMAL)));
            prodCell.addElement(new Paragraph("Kód: " + code + "   EAN: " + ean, getSlovakFont(8, Font.BOLD)));
            itemsTable.addCell(prodCell);

            itemsTable.addCell(createRightAlignedCell(basePrice.toString() + " €"));
            itemsTable.addCell(createRightAlignedCell("20 %"));
            itemsTable.addCell(createRightAlignedCell(vatAmount.toString() + " €"));
            itemsTable.addCell(createRightAlignedCell(priceWithVat.toString() + " €"));

            PdfPCell qtyCell = createRightAlignedCell(String.valueOf(item.getQuantity()));
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Usually center
            itemsTable.addCell(qtyCell);

            itemsTable.addCell(createRightAlignedCell(lineTotal.toString() + " €"));
        }

        // Shipping Cost Row if > 0
        if (order.getDeliveryPrice() != null && order.getDeliveryPrice().compareTo(BigDecimal.ZERO) > 0) {
             BigDecimal priceWithVat = order.getDeliveryPrice();
             BigDecimal basePrice = priceWithVat.divide(BigDecimal.ONE.add(vatRate), 2, RoundingMode.HALF_UP);
             BigDecimal vatAmount = priceWithVat.subtract(basePrice);

             totalWithVat = totalWithVat.add(priceWithVat);
             totalBase = totalBase.add(basePrice);
             totalVat = totalVat.add(vatAmount);

             itemsTable.addCell(new Paragraph("Poplatky za dopravu", getSlovakFont(8, Font.NORMAL)));
             itemsTable.addCell(createRightAlignedCell(basePrice.toString() + " €"));
             itemsTable.addCell(createRightAlignedCell("20 %"));
             itemsTable.addCell(createRightAlignedCell(vatAmount.toString() + " €"));
             itemsTable.addCell(createRightAlignedCell(priceWithVat.toString() + " €"));
             itemsTable.addCell(createRightAlignedCell("1"));
             itemsTable.addCell(createRightAlignedCell(priceWithVat.toString() + " €"));
        }

        document.add(itemsTable);

        // Footer / Totals
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(20);
        footerTable.setWidths(new float[]{1.5f, 1});

        // Left Side (VAT Breakdown)
        PdfPTable vatTable = new PdfPTable(4);
        vatTable.setWidthPercentage(100);
        vatTable.addCell(createHeaderCell("Sadzba DPH"));
        vatTable.addCell(createHeaderCell("Celkom bez DPH"));
        vatTable.addCell(createHeaderCell("Celkom DPH"));
        vatTable.addCell(createHeaderCell("Spolu"));

        vatTable.addCell(createRightAlignedCell("20 %"));
        vatTable.addCell(createRightAlignedCell(totalBase.toString() + " €"));
        vatTable.addCell(createRightAlignedCell(totalVat.toString() + " €"));
        vatTable.addCell(createRightAlignedCell(totalWithVat.toString() + " €"));

        PdfPCell vatWrapper = new PdfPCell(vatTable);
        vatWrapper.setBorder(Rectangle.NO_BORDER);
        vatWrapper.setBackgroundColor(new Color(245, 245, 245)); // Light gray bg
        footerTable.addCell(vatWrapper);

        // Right Side (Final Total)
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);

        totalTable.addCell(createLabelCell("Celkom (bez DPH)"));
        totalTable.addCell(createBoldRightCell(totalBase.toString() + " €"));

        totalTable.addCell(createLabelCell("Celkom DPH"));
        totalTable.addCell(createBoldRightCell(totalVat.toString() + " €"));

        totalTable.addCell(createLabelCell("Celkom"));
        totalTable.addCell(createBoldRightCell(totalWithVat.toString() + " €", 14)); // Larger font

        PdfPCell totalWrapper = new PdfPCell(totalTable);
        totalWrapper.setBorder(Rectangle.BOX);
        totalWrapper.setPadding(10);
        footerTable.addCell(totalWrapper);

        document.add(footerTable);
    }

    private void addDateCell(PdfPTable table, String label, java.time.Instant date, boolean boldValue) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX); // Or specific borders
        cell.addElement(new Paragraph(label, getSlovakFont(9, boldValue ? Font.BOLD : Font.NORMAL))); // Actually label usually distinct color

        String dateStr = "";
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
            dateStr = formatter.format(date);
        }
        cell.addElement(new Paragraph(dateStr, getSlovakFont(10, Font.BOLD)));
        table.addCell(cell);
    }

    private PdfPCell createRightAlignedCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(8, Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(8, Font.BOLD)));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell createBoldRightCell(String text) {
        return createBoldRightCell(text, 10);
    }

    private PdfPCell createBoldRightCell(String text, int size) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, getSlovakFont(size, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}
