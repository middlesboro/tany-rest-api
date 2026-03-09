package sk.tany.rest.api.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.domain.supplier.SupplierInvoice;
import sk.tany.rest.api.service.MistralOcrService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class MistralOcrServiceImpl implements MistralOcrService {

    private final String apiKey;
    private final ChatModel chatModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MistralOcrServiceImpl(@Value("${langchain4j.mistral.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("mistral-large-latest")
                .temperature(0.0)
                .build();
    }

    @Override
    public SupplierInvoice extractInvoiceData(byte[] pdfBytes, String fileName) {
        String markdown = performOcr(pdfBytes);
        if (markdown == null || markdown.isBlank()) {
            log.error("Mistral OCR returned empty result");
            return null;
        }

        return extractDataFromMarkdown(markdown);
    }

    private String performOcr(byte[] pdfBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String base64 = Base64.getEncoder().encodeToString(pdfBytes);

            JsonObject document = new JsonObject();
            document.addProperty("type", "document_url");
            document.addProperty("document_url", "data:application/pdf;base64," + base64);

            JsonObject body = new JsonObject();
            body.addProperty("model", "mistral-ocr-latest");
            body.add("document", document);

            String requestBody = new Gson().toJson(body);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<MistralOcrResponse> response = restTemplate.exchange(
                    "https://api.mistral.ai/v1/ocr",
                    HttpMethod.POST,
                    requestEntity,
                    MistralOcrResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getPages() != null) {
                StringBuilder sb = new StringBuilder();
                for (MistralOcrResponse.Page page : response.getBody().getPages()) {
                    sb.append(page.getMarkdown()).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.error("Exception calling Mistral OCR", e);
        }
        return null;
    }

    private SupplierInvoice extractDataFromMarkdown(String markdown) {
        String prompt = "You are an expert at extracting structured data from invoice markdown texts. " +
                "Extract the following information from the provided text and return ONLY a valid JSON object. " +
                "Do not include markdown code blocks or any other text.\n\n" +
                "{\n" +
                "  \"supplierName\": \"string\",\n" +
                "  \"priceWithVat\": \"number\",\n" +
                "  \"priceWithoutVat\": \"number\",\n" +
                "  \"vatValue\": \"number\",\n" +
                "  \"dateCreated\": \"YYYY-MM-DD\",\n" +
                "  \"supplierVatIdentifier\": \"string\",\n" +
                "  \"taxDate\": \"YYYY-MM-DD\",\n" +
                "  \"invoiceNumber\": \"string\",\n" +
                "  \"paymentReference\": \"string (variabilny symbol)\"\n" +
                "}\n\n" +
                "Text:\n" + markdown;

        try {
            String jsonResponse = chatModel.chat(prompt);

            // Cleanup response if it has markdown formatting
            jsonResponse = jsonResponse.trim();
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            } else if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }

            ExtractedData data = objectMapper.readValue(jsonResponse, ExtractedData.class);
            return mapToInvoice(data);
        } catch (Exception e) {
            log.error("Failed to extract data using Mistral Chat Model", e);
            return null;
        }
    }

    private SupplierInvoice mapToInvoice(ExtractedData data) {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setSupplierName(data.getSupplierName());

        if (data.getPriceWithVat() != null) invoice.setPriceWithVat(new BigDecimal(data.getPriceWithVat()));
        if (data.getPriceWithoutVat() != null) invoice.setPriceWithoutVat(new BigDecimal(data.getPriceWithoutVat()));
        if (data.getVatValue() != null) invoice.setVatValue(new BigDecimal(data.getVatValue()));

        invoice.setDateCreated(parseDate(data.getDateCreated()));
        invoice.setTaxDate(parseDate(data.getTaxDate()));

        invoice.setSupplierVatIdentifier(data.getSupplierVatIdentifier());
        invoice.setInvoiceNumber(data.getInvoiceNumber());
        invoice.setPaymentReference(data.getPaymentReference());

        return invoice;
    }

    private Instant parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    @Data
    public static class MistralFileResponse {
        private String id;
        private String object;
    }

    @Data
    public static class MistralOcrResponse {
        private List<Page> pages;

        @Data
        public static class Page {
            private String markdown;
        }
    }

    @Data
    public static class ExtractedData {
        private String supplierName;
        private String priceWithVat;
        private String priceWithoutVat;
        private String vatValue;
        private String dateCreated;
        private String supplierVatIdentifier;
        private String taxDate;
        private String invoiceNumber;
        private String paymentReference;
    }
}
