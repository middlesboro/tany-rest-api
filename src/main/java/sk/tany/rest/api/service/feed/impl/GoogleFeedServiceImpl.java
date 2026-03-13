package sk.tany.rest.api.service.feed.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.service.feed.GoogleFeedService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleFeedServiceImpl implements GoogleFeedService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    private final sk.tany.rest.api.config.EshopConfig eshopConfig;

    private static final String FEED_DIR = "google-feeds";
    private static final String PRODUCT_FEED_FILE = "google_products.xml";

    @Override
    @Transactional(readOnly = true)
    public synchronized void generateProductFeed() {
        log.info("Starting Google product feed generation...");

        Map<String, String> brandMap = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName, (a, b) -> a));

        Map<String, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        File finalFile = getFile(PRODUCT_FEED_FILE, true);
        File tempFile = new File(finalFile.getParent(), finalFile.getName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));
             Stream<Product> productStream = productRepository.streamAllByActiveTrue()) {

            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            writer.write("<rss version=\"2.0\" xmlns:g=\"http://base.google.com/ns/1.0\">\n");
            writer.write("  <channel>\n");
            writer.write("    <title>Google Merchant Feed</title>\n");
            writer.write("    <link>" + eshopConfig.getFrontendUrl() + "</link>\n");
            writer.write("    <description>Product feed for Google Merchant Center</description>\n");

            productStream.forEach(product -> {
                try {
                    writer.write("    <item>\n");
                    appendTag(writer, "g:id", product.getProductIdentifier() != null ? product.getProductIdentifier().toString() : product.getId());
                    appendTag(writer, "g:title", product.getTitle());
                    appendTag(writer, "g:description", product.getDescription() != null ? product.getDescription() : product.getShortDescription());
                    appendTag(writer, "g:link", eshopConfig.getFrontendUrl() + "/produkt/" + product.getSlug());

                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        appendTag(writer, "g:image_link", product.getImages().get(0));
                        for (int i = 1; i < product.getImages().size() && i <= 10; i++) {
                            appendTag(writer, "g:additional_image_link", product.getImages().get(i));
                        }
                    }

                    int quantity = product.getQuantity() != null ? product.getQuantity() : 0;
                    String availability = quantity > 0 ? "in_stock" : "out_of_stock";
                    appendTag(writer, "g:availability", availability);

                    BigDecimal price = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
                    if (price != null) {
                        appendTag(writer, "g:price", String.format("%.2f EUR", price).replace(",", "."));
                    }

                    if (product.getBrandId() != null && brandMap.containsKey(product.getBrandId())) {
                        appendTag(writer, "g:brand", brandMap.get(product.getBrandId()));
                    } else {
                        appendTag(writer, "g:brand", "Tany");
                    }

                    if (product.getEan() != null && !product.getEan().isEmpty()) {
                        appendTag(writer, "g:gtin", product.getEan());
                    }

                    if (product.getProductCode() != null && !product.getProductCode().isEmpty()) {
                        appendTag(writer, "g:mpn", product.getProductCode());
                    } else if (product.getProductIdentifier() != null) {
                        appendTag(writer, "g:mpn", product.getProductIdentifier().toString());
                    }

                    appendTag(writer, "g:condition", "new");

                    String productType = getProductType(product, categoryMap);
                    if (productType != null) {
                        appendTag(writer, "g:product_type", productType);
                    }

                    writer.write("    </item>\n");
                } catch (IOException e) {
                    log.error("Error writing product to Google feed: {}", product.getId(), e);
                }
            });

            writer.write("  </channel>\n");
            writer.write("</rss>");
        } catch (IOException e) {
            log.error("Failed to generate Google product feed", e);
            throw new RuntimeException("Failed to generate Google product feed", e);
        }

        if (tempFile.exists()) {
            if (!tempFile.renameTo(finalFile)) {
                log.error("Failed to rename temp file {} to {}", tempFile.getAbsolutePath(), finalFile.getAbsolutePath());
                // Fallback: delete final and rename
                if (finalFile.delete() && tempFile.renameTo(finalFile)) {
                    log.info("Renamed temp file after deleting original.");
                } else {
                    log.error("Critical error: Could not replace Google product feed file.");
                }
            } else {
                log.info("Google product feed generated successfully.");
            }
        }
    }

    @Override
    public File getProductFeedFile() {
        return getFile(PRODUCT_FEED_FILE, false);
    }

    private void appendTag(BufferedWriter writer, String tagName, String value) throws IOException {
        if (value != null && !value.isEmpty()) {
            writer.write("      <" + tagName + ">");
            writer.write(StringEscapeUtils.escapeXml11(value));
            writer.write("</" + tagName + ">\n");
        }
    }

    private String getProductType(Product product, Map<String, Category> categoryMap) {
        String categoryId = product.getDefaultCategoryId();
        if (categoryId == null && product.getCategoryIds() != null && !product.getCategoryIds().isEmpty()) {
            categoryId = product.getCategoryIds().get(0);
        }

        if (categoryId == null) return null;

        Category category = categoryMap.get(categoryId);
        if (category == null) return null;

        List<String> path = new java.util.ArrayList<>();
        while (category != null) {
            path.add(0, category.getTitle());
            if (category.getParentId() != null) {
                category = categoryMap.get(category.getParentId());
            } else {
                category = null;
            }
        }
        return String.join(" > ", path);
    }

    private File getFile(String filename, boolean createDir) {
        File dir = new File(System.getProperty("java.io.tmpdir"), FEED_DIR);
        if (createDir && !dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, filename);
        if (!createDir && !file.exists()) {
            return null;
        }
        return file;
    }
}
