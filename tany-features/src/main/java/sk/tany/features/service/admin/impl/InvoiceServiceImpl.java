package sk.tany.features.service.admin.impl;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import sk.tany.features.dto.InvoiceDataDto;
import sk.tany.features.service.admin.InvoiceService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final Color BRAND_COLOR = new Color(52, 73, 94); // Dark blue-gray
    private static final Color ACCENT_COLOR = new Color(245, 247, 250); // Light gray background
    private static final String FONT_PATH = "/fonts/Roboto-Regular.ttf";
    private static final String FONT_BOLD_PATH = "/fonts/Roboto-Bold.ttf";

    @Override
    public byte[] generateInvoice(InvoiceDataDto invoiceData) {
        return generatePdf(invoiceData, false);
    }

    @Override
    public byte[] generateCreditNote(InvoiceDataDto invoiceData) {
        return generatePdf(invoiceData, true);
    }

    private byte[] generatePdf(InvoiceDataDto invoiceData, boolean isCreditNote) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new InvoiceFooter(invoiceData));
            document.open();

            // Header Section
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1});

            // Logo
            try {
                Image logo = Image.getInstance(getClass().getResource("/logo.png"));
                logo.scaleToFit(150, 50);
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                headerTable.addCell(logoCell);
            } catch (Exception e) {
                PdfPCell logoCell = new PdfPCell(new Paragraph("TANY", getSlovakFont(24, Font.BOLD, BRAND_COLOR)));
                logoCell.setBorder(Rectangle.NO_BORDER);
                headerTable.addCell(logoCell);
            }

            // Document Title
            String prefix = isCreditNote ? "Dobropis k fak. č.: " : "Faktúra č.: ";
            String title = prefix + invoiceData.getOrderIdentifier();
            PdfPCell titleCell = new PdfPCell(new Paragraph(title, getSlovakFont(16, Font.BOLD, BRAND_COLOR)));
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(titleCell);

            document.add(headerTable);

            // Divider
            Paragraph divider = new Paragraph("");
            divider.setSpacingBefore(20);
            document.add(divider);

            // Addresses Section
            PdfPTable addressTable = new PdfPTable(2);
            addressTable.setWidthPercentage(100);
            addressTable.setSpacingBefore(10);
            addressTable.setWidths(new float[]{1, 1});

            // Supplier Address
            PdfPCell supplierCell = new PdfPCell();
            supplierCell.setBorder(Rectangle.NO_BORDER);
            supplierCell.setPaddingRight(20);
            supplierCell.addElement(new Paragraph("Dodávateľ:", getSlovakFont(10, Font.BOLD, BRAND_COLOR)));

            String supplierName = invoiceData.getCompanyName();
            if (supplierName == null || supplierName.isEmpty()) supplierName = "Tany.sk";

            supplierCell.addElement(new Paragraph(supplierName, getSlovakFont(11, Font.BOLD)));

            String addressLine1 = invoiceData.getShopStreet() != null ? invoiceData.getShopStreet() : "";
            String addressLine2 = (invoiceData.getShopZip() != null ? invoiceData.getShopZip() + " " : "") +
                                  (invoiceData.getShopCity() != null ? invoiceData.getShopCity() : "");

            supplierCell.addElement(new Paragraph(addressLine1, getSlovakFont(10, Font.NORMAL)));
            supplierCell.addElement(new Paragraph(addressLine2, getSlovakFont(10, Font.NORMAL)));

            supplierCell.addElement(new Paragraph("IČO: " + (invoiceData.getShopIco() != null ? invoiceData.getShopIco() : ""), getSlovakFont(10, Font.NORMAL)));
            supplierCell.addElement(new Paragraph("DIČ: " + (invoiceData.getShopDic() != null ? invoiceData.getShopDic() : ""), getSlovakFont(10, Font.NORMAL)));
            supplierCell.addElement(new Paragraph("IČ DPH: " + (invoiceData.getShopIcdph() != null ? invoiceData.getShopIcdph() : ""), getSlovakFont(10, Font.NORMAL)));
            supplierCell.addElement(new Paragraph("\nIBAN: " + (invoiceData.getShopIban() != null ? invoiceData.getShopIban() : ""), getSlovakFont(10, Font.BOLD)));
            addressTable.addCell(supplierCell);

            // Customer Address
            PdfPCell customerCell = new PdfPCell();
            customerCell.setBorder(Rectangle.NO_BORDER);
            customerCell.setBackgroundColor(ACCENT_COLOR);
            customerCell.setPadding(15);

            customerCell.addElement(new Paragraph("Odberateľ:", getSlovakFont(10, Font.BOLD, BRAND_COLOR)));

            String custName = invoiceData.getCompany() != null && !invoiceData.getCompany().isEmpty()
                ? invoiceData.getCompany()
                : invoiceData.getBillingFirstname() + " " + invoiceData.getBillingLastname();

            customerCell.addElement(new Paragraph(custName, getSlovakFont(11, Font.BOLD)));
            customerCell.addElement(new Paragraph(invoiceData.getBillingStreet(), getSlovakFont(10, Font.NORMAL)));
            customerCell.addElement(new Paragraph(invoiceData.getBillingZip() + " " + invoiceData.getBillingCity(), getSlovakFont(10, Font.NORMAL)));
            customerCell.addElement(new Paragraph(invoiceData.getBillingCountry(), getSlovakFont(10, Font.NORMAL)));

            if (invoiceData.getIco() != null && !invoiceData.getIco().isEmpty()) {
                customerCell.addElement(new Paragraph("\nIČO: " + invoiceData.getIco(), getSlovakFont(10, Font.NORMAL)));
            }
            if (invoiceData.getDic() != null && !invoiceData.getDic().isEmpty()) {
                customerCell.addElement(new Paragraph("DIČ: " + invoiceData.getDic(), getSlovakFont(10, Font.NORMAL)));
            }
            if (invoiceData.getIcdph() != null && !invoiceData.getIcdph().isEmpty()) {
                customerCell.addElement(new Paragraph("IČ DPH: " + invoiceData.getIcdph(), getSlovakFont(10, Font.NORMAL)));
            }
            addressTable.addCell(customerCell);

            document.add(addressTable);

            // Order Details (Dates, Payment, Shipping)
            PdfPTable detailsTable = new PdfPTable(4);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(20);
            detailsTable.setWidths(new float[]{1, 1, 1, 1});

            String issueDate = formatDate(invoiceData.getCreateDate());
            addDetailCell(detailsTable, "Dátum vystavenia:", issueDate);
            addDetailCell(detailsTable, "Dátum dodania:", issueDate);

            String dueDate = invoiceData.getPaymentNotificationDate() != null
                ? formatDate(invoiceData.getPaymentNotificationDate())
                : issueDate;

            addDetailCell(detailsTable, "Dátum splatnosti:", dueDate);
            addDetailCell(detailsTable, "Spôsob úhrady:", invoiceData.getPaymentName() != null ? invoiceData.getPaymentName() : "-");

            document.add(detailsTable);

            // Order Items
            PdfPTable itemsTable = new PdfPTable(6);
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingBefore(30);
            itemsTable.setWidths(new float[]{4, 1.5f, 1, 1.5f, 1, 1.5f});
            itemsTable.setHeaderRows(1);

            String[] headers = {"POPIS", "CENA", "DPH %", "DPH", "KS", "SPOLU"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, getSlovakFont(8, Font.BOLD, BRAND_COLOR)));
                cell.setBackgroundColor(ACCENT_COLOR);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(BRAND_COLOR);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemsTable.addCell(cell);
            }

            if (invoiceData.getItems() != null) {
                boolean alternate = false;
                BigDecimal globalMultiplier = isCreditNote ? new BigDecimal("-1") : BigDecimal.ONE;

                for (InvoiceDataDto.InvoiceItemDto item : invoiceData.getItems()) {
                    String name = item.getTitle() != null ? item.getTitle() : "";
                    if (item.getVariantTitle() != null && !item.getVariantTitle().isEmpty()) {
                        name += " - " + item.getVariantTitle();
                    }
                    String code = item.getProductCode() != null ? item.getProductCode() : "";

                    BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;

                    // Simplify: assume price in dto is with VAT, and VAT is 23%
                    // In a real app we would pass base price and vat value directly in the DTO, but for simplicity:
                    BigDecimal vatRateValue = new BigDecimal("23");
                    BigDecimal vatDivider = BigDecimal.ONE.add(vatRateValue.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

                    BigDecimal priceWithVat = itemPrice;
                    BigDecimal priceWithoutVat = priceWithVat.divide(vatDivider, 2, RoundingMode.HALF_UP);
                    BigDecimal vatValue = priceWithVat.subtract(priceWithoutVat);

                    BigDecimal qty = item.getQuantity() != null && item.getQuantity() > 0 ? new BigDecimal(item.getQuantity()) : BigDecimal.ONE;

                    BigDecimal lineTotalWithVat = priceWithVat.multiply(qty).multiply(globalMultiplier);
                    BigDecimal lineTotalVat = vatValue.multiply(qty).multiply(globalMultiplier);
                    BigDecimal unitPriceBase = priceWithoutVat.multiply(globalMultiplier);

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
                    itemsTable.addCell(createItemCell("23%", rowColor));
                    itemsTable.addCell(createItemCell(lineTotalVat.toString() + " €", rowColor));
                    itemsTable.addCell(createItemCell(String.valueOf(item.getQuantity()), rowColor));
                    itemsTable.addCell(createItemCell(lineTotalWithVat.toString() + " €", rowColor, Font.BOLD));

                    alternate = !alternate;
                }
            }

            document.add(itemsTable);

            // Footer / Totals
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setSpacingBefore(20);
            footerTable.setWidths(new float[]{1.5f, 1});

            BigDecimal multiplier = isCreditNote ? new BigDecimal("-1") : BigDecimal.ONE;
            BigDecimal totalWithVat = invoiceData.getTotalPrice() != null ? invoiceData.getTotalPrice().multiply(multiplier) : BigDecimal.ZERO;

            // Recalculate base and vat from total for simplicity in DTO
            BigDecimal vatRateValue = new BigDecimal("23");
            BigDecimal vatDivider = BigDecimal.ONE.add(vatRateValue.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            BigDecimal totalBase = totalWithVat.divide(vatDivider, 2, RoundingMode.HALF_UP);
            BigDecimal totalVat = totalWithVat.subtract(totalBase);

            PdfPCell leftSpace = new PdfPCell(new Paragraph(""));
            leftSpace.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(leftSpace);

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

            if (!isCreditNote && "PAID".equalsIgnoreCase(invoiceData.getStatus())) {
                Paragraph paidNote = new Paragraph("FAKTÚRA JE UŽ UHRADENÁ", getSlovakFont(12, Font.BOLD, BRAND_COLOR));
                paidNote.setAlignment(Element.ALIGN_CENTER);
                paidNote.setSpacingBefore(10);
                document.add(paidNote);
            }

            document.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private Font getSlovakFont(int size, int style) {
        return getSlovakFont(size, style, Color.BLACK);
    }

    private Font getSlovakFont(int size, int style, Color color) {
        try {
            String fontPath = style == Font.BOLD ? FONT_BOLD_PATH : FONT_PATH;
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style, color);
        } catch (Exception e) {
            return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
        }
    }

    private class InvoiceFooter extends PdfPageEventHelper {
        private final InvoiceDataDto invoiceData;

        public InvoiceFooter(InvoiceDataDto invoiceData) {
            this.invoiceData = invoiceData;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String email = invoiceData.getShopEmail() != null ? invoiceData.getShopEmail() : "";
            String phone = invoiceData.getShopPhone() != null ? invoiceData.getShopPhone() : "";
            String footerText = "Tany.sk | Email: " + email + " | Tel: " + phone;
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
        cell.addElement(new Paragraph(value != null ? value : "", getSlovakFont(10, Font.NORMAL)));
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
