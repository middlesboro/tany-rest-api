package sk.tany.rest.api.service.admin;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateResponse;
import sk.tany.rest.api.dto.admin.product.create.ProductImportUrlAiResponse;
import sk.tany.rest.api.dto.admin.product.create.ProductImportUrlRequest;
import sk.tany.rest.api.service.chat.ProductImportAiAgent;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportUrlService {

    private final ProductImportAiAgent productImportAiAgent;

    public ProductCreateResponse importFromUrl(ProductImportUrlRequest request) {
        try {
            // Validate URL protocol
            if (!request.getUrl().toLowerCase().startsWith("http://") && !request.getUrl().toLowerCase().startsWith("https://")) {
                throw new IllegalArgumentException("URL must start with http:// or https://");
            }

            // 1. Fetch HTML
            Document document = Jsoup.connect(request.getUrl()).get();

            // 2. Clean HTML
            // Remove non-content tags
            document.select("script, style, header, footer, nav, aside, noscript, iframe, link, meta").remove();

            // Keep basic formatting, links, and images so AI can extract descriptions and image URLs.
            Safelist safelist = Safelist.basicWithImages().addTags("h1", "h2", "h3", "h4", "h5", "h6", "div", "span", "form");
            String cleanHtml = Jsoup.clean(document.body().html(), request.getUrl(), safelist);

            log.info("Extracted html length from URL {}: {}", request.getUrl(), cleanHtml.length());

            // Convert to Markdown to save tokens and give structured text to the AI
            FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
            String markdown = converter.convert(cleanHtml);

            log.info("Converted Markdown length from URL {}: {}", request.getUrl(), markdown.length());

            // 3. Send to AI to extract data
            ProductImportUrlAiResponse aiResponse = productImportAiAgent.extractProductData(markdown);

            // 4. Map to ProductCreateResponse
            return mapToCreateResponse(aiResponse, request);

        } catch (IOException e) {
            log.error("Failed to fetch or parse URL: {}", request.getUrl(), e);
            throw new RuntimeException("Failed to fetch product data from URL", e);
        }
    }

    private ProductCreateResponse mapToCreateResponse(ProductImportUrlAiResponse aiResponse, ProductImportUrlRequest request) {
        ProductCreateResponse response = new ProductCreateResponse();

        if (aiResponse != null) {
            response.setTitle(aiResponse.getTitle());
            response.setShortDescription(aiResponse.getShortDescription());
            response.setDescription(aiResponse.getDescription());
            response.setPrice(aiResponse.getPrice());
            response.setWeight(aiResponse.getWeight());
            response.setQuantity(aiResponse.getQuantity());
            response.setMetaTitle(aiResponse.getMetaTitle());
            response.setMetaDescription(aiResponse.getMetaDescription());
            response.setProductCode(aiResponse.getProductCode());
            response.setEan(aiResponse.getEan());
            response.setImages(aiResponse.getImages());
        }

        response.setBrandId(request.getBrandId());
        response.setSupplierId(request.getSupplierId());

        return response;
    }
}
