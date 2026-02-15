package sk.tany.rest.api.service.feed.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.service.feed.HeurekaFeedService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeurekaFeedServiceImpl implements HeurekaFeedService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    private static final String FEED_DIR = "heureka-feeds";
    private static final String PRODUCT_FEED_FILE = "heureka_products.xml";
    private static final String AVAILABILITY_FEED_FILE = "heureka_availability.xml";

    @Override
    @Transactional(readOnly = true)
    public synchronized void generateProductFeed() {
        log.info("Starting Heureka product feed generation...");

        Map<String, String> brandMap = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName, (a, b) -> a));

        Map<String, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        File finalFile = getFile(PRODUCT_FEED_FILE, true);
        File tempFile = new File(finalFile.getParent(), finalFile.getName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));
             Stream<Product> productStream = productRepository.streamAllByActiveTrue()) {

            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            writer.write("<SHOP>\n");

            productStream.forEach(product -> {
                try {
                    writer.write("  <SHOPITEM>\n");
                    appendTag(writer, "ITEM_ID", product.getProductIdentifier() != null ? product.getProductIdentifier().toString() : product.getId());
                    appendTag(writer, "PRODUCTNAME", product.getTitle());
                    appendTag(writer, "PRODUCT", product.getTitle());
                    appendTag(writer, "DESCRIPTION", product.getDescription() != null ? product.getDescription() : product.getShortDescription());
                    appendTag(writer, "URL", frontendUrl + "/produkt/" + product.getSlug());

                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                         appendTag(writer, "IMGURL", product.getImages().get(0));
                         for (int i = 1; i < product.getImages().size(); i++) {
                             appendTag(writer, "IMGURL_ALTERNATIVE", product.getImages().get(i));
                         }
                    }

                    BigDecimal price = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
                    if (price != null) {
                        appendTag(writer, "PRICE_VAT", String.format("%.2f", price).replace(",", "."));
                    }

                    if (product.getBrandId() != null && brandMap.containsKey(product.getBrandId())) {
                        appendTag(writer, "MANUFACTURER", brandMap.get(product.getBrandId()));
                    }

                    String categoryText = getCategoryText(product, categoryMap);
                    if (categoryText != null) {
                        appendTag(writer, "CATEGORYTEXT", categoryText);
                    }

                    if (product.getEan() != null) {
                        appendTag(writer, "EAN", product.getEan());
                    }

                    if (product.getProductCode() != null) {
                        appendTag(writer, "PRODUCTNO", product.getProductCode());
                    }

                    int deliveryDate = (product.getQuantity() != null && product.getQuantity() > 0) ? 0 : 7;
                    appendTag(writer, "DELIVERY_DATE", String.valueOf(deliveryDate));

                    writer.write("  </SHOPITEM>\n");
                } catch (IOException e) {
                    log.error("Error writing product to feed: {}", product.getId(), e);
                }
            });

            writer.write("</SHOP>");
        } catch (IOException e) {
            log.error("Failed to generate product feed", e);
            throw new RuntimeException("Failed to generate product feed", e);
        }

        if (tempFile.exists()) {
            if (!tempFile.renameTo(finalFile)) {
                log.error("Failed to rename temp file {} to {}", tempFile.getAbsolutePath(), finalFile.getAbsolutePath());
                // Fallback: delete final and rename
                if (finalFile.delete() && tempFile.renameTo(finalFile)) {
                    log.info("Renamed temp file after deleting original.");
                } else {
                     log.error("Critical error: Could not replace product feed file.");
                }
            } else {
                log.info("Heureka product feed generated successfully.");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public synchronized void generateAvailabilityFeed() {
        log.info("Starting Heureka availability feed generation...");

        File finalFile = getFile(AVAILABILITY_FEED_FILE, true);
        File tempFile = new File(finalFile.getParent(), finalFile.getName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));
             Stream<Product> productStream = productRepository.streamAllByActiveTrue()) {

            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            writer.write("<item_list>\n");

            productStream.forEach(product -> {
                try {
                    String itemId = product.getProductIdentifier() != null ? product.getProductIdentifier().toString() : product.getId();
                    writer.write("  <item id=\"" + StringEscapeUtils.escapeXml11(itemId) + "\">\n");

                    int quantity = product.getQuantity() != null ? product.getQuantity() : 0;
                    writer.write("    <stock_quantity>" + quantity + "</stock_quantity>\n");

                    int deliveryTime = (quantity > 0) ? 0 : 7;
                    writer.write("    <delivery_time>" + deliveryTime + "</delivery_time>\n");

                    writer.write("  </item>\n");
                } catch (IOException e) {
                    log.error("Error writing product to availability feed: {}", product.getId(), e);
                }
            });

            writer.write("</item_list>");
        } catch (IOException e) {
            log.error("Failed to generate availability feed", e);
            throw new RuntimeException("Failed to generate availability feed", e);
        }

        if (tempFile.exists()) {
            if (!tempFile.renameTo(finalFile)) {
                log.error("Failed to rename temp file {} to {}", tempFile.getAbsolutePath(), finalFile.getAbsolutePath());
                if (finalFile.delete() && tempFile.renameTo(finalFile)) {
                    log.info("Renamed temp file after deleting original.");
                } else {
                     log.error("Critical error: Could not replace availability feed file.");
                }
            } else {
                log.info("Heureka availability feed generated successfully.");
            }
        }
    }

    @Override
    public File getProductFeedFile() {
        return getFile(PRODUCT_FEED_FILE, false);
    }

    @Override
    public File getAvailabilityFeedFile() {
        return getFile(AVAILABILITY_FEED_FILE, false);
    }

    private void appendTag(BufferedWriter writer, String tagName, String value) throws IOException {
        if (value != null && !value.isEmpty()) {
            writer.write("    <" + tagName + ">");
            writer.write(StringEscapeUtils.escapeXml11(value));
            writer.write("</" + tagName + ">\n");
        }
    }

    private String getCategoryText(Product product, Map<String, Category> categoryMap) {
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
        return String.join(" | ", path);
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
