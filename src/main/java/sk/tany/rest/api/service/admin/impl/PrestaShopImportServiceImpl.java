package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.prestashop.PrestaShopCategory;
import sk.tany.rest.api.dto.prestashop.PrestaShopImage;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductDetailResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductWrapper;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductsResponse;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.admin.ProductAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrestaShopImportServiceImpl implements PrestaShopImportService {

    private final RestTemplate restTemplate;
    private final ProductAdminService productAdminService;
    private final ImageService imageService;

    @Value("${prestashop.url}")
    private String prestashopUrl;

    @Value("${prestashop.key}")
    private String prestashopKey;

    @Override
    public void importAllProducts() {
        log.info("Starting import of all products from PrestaShop");
        String url = String.format("%s/api/products?ws_key=%s&output_format=JSON", prestashopUrl, prestashopKey);
        try {
            PrestaShopProductsResponse response = restTemplate.getForObject(url, PrestaShopProductsResponse.class);
            if (response != null && response.getProducts() != null) {
                for (PrestaShopProductResponse product : response.getProducts()) {
                    try {
                        importProduct(String.valueOf(product.getId()));
                    } catch (Exception e) {
                        log.error("Failed to import product with ID: {}", product.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching product list from PrestaShop", e);
            throw new RuntimeException("Error fetching product list from PrestaShop", e);
        }
        log.info("Finished import of all products from PrestaShop");
    }

    @Override
    public void importProduct(String id) {
        log.info("Importing product with ID: {}", id);
        String url = String.format("%s/api/products/%s?ws_key=%s&output_format=JSON", prestashopUrl, id, prestashopKey);
        try {
            PrestaShopProductWrapper wrapper = restTemplate.getForObject(url, PrestaShopProductWrapper.class);
            if (wrapper != null && wrapper.getProduct() != null) {
                ProductDto productDto = mapToProductDto(wrapper.getProduct());
                productAdminService.save(productDto);
                log.info("Successfully imported product with ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error importing product with ID: {}", id, e);
            throw new RuntimeException("Error importing product with ID: " + id, e);
        }
    }

    private ProductDto mapToProductDto(PrestaShopProductDetailResponse psProduct) {
        ProductDto dto = new ProductDto();
        dto.setTitle(parseLanguageValue(psProduct.getName()));
        dto.setDescription(parseLanguageValue(psProduct.getDescription()));
        dto.setShortDescription(parseLanguageValue(psProduct.getDescriptionShort()));
        dto.setPrice(psProduct.getPrice());
        dto.setWholesalePrice(psProduct.getWholesalePrice());
        dto.setWeight(psProduct.getWeight());
        dto.setProductCode(psProduct.getReference());
        dto.setEan(psProduct.getEan13());

        dto.setStatus("1".equals(psProduct.getActive()) ? ProductStatus.AVAILABLE : ProductStatus.SOLD_OUT);
        dto.setCategoryIds(getCategoryIds(psProduct));

        List<String> imageUrls = new ArrayList<>();
        if (psProduct.getAssociations() != null && psProduct.getAssociations().getImages() != null) {
            for (PrestaShopImage psImage : psProduct.getAssociations().getImages()) {
                String imgUrl = downloadAndUploadImage(psProduct.getId(), psImage.getId());
                if (imgUrl != null) {
                    imageUrls.add(imgUrl);
                }
            }
        }
        dto.setImages(imageUrls);

        return dto;
    }

    private static List<String> getCategoryIds(PrestaShopProductDetailResponse psProduct) {
        List<String> categoryIds = new ArrayList<>();
        if (psProduct.getCategoryIdDefault() != null) {
            categoryIds.add(psProduct.getCategoryIdDefault());
        }
        if (psProduct.getAssociations() != null && psProduct.getAssociations().getCategories() != null) {
             for(PrestaShopCategory cat : psProduct.getAssociations().getCategories()) {
                 if(!categoryIds.contains(cat.getId())) {
                     categoryIds.add(cat.getId());
                 }
             }
        }
        return categoryIds;
    }

    private String parseLanguageValue(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (!list.isEmpty()) {
                Object firstItem = list.get(0);
                if (firstItem instanceof Map) {
                    return (String) ((Map<?, ?>) firstItem).get("value");
                }
            }
        }
        return "";
    }

    private String downloadAndUploadImage(Long productId, String imageId) {
        String imageUrl = String.format("%s/api/images/products/%d/%s?ws_key=%s", prestashopUrl, productId, imageId, prestashopKey);
        try {
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes != null && imageBytes.length > 0) {
                return imageService.upload(imageBytes, "ps_product_" + productId + "_" + imageId + ".jpg", ImageKitType.PRODUCT);
            }
        } catch (Exception e) {
            log.error("Failed to download/upload image {} for product {}", imageId, productId, e);
        }
        return null;
    }
}
