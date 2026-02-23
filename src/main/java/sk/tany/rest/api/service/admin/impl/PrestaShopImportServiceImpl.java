package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopCategoriesResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopCategory;
import sk.tany.rest.api.dto.prestashop.PrestaShopCategoryDetailResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopCategoryResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopCategoryWrapper;
import sk.tany.rest.api.dto.prestashop.PrestaShopImage;
import sk.tany.rest.api.dto.prestashop.PrestaShopManufacturerDetailResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopManufacturerResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopManufacturerWrapper;
import sk.tany.rest.api.dto.prestashop.PrestaShopManufacturersResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductDetailResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductWrapper;
import sk.tany.rest.api.dto.prestashop.PrestaShopProductsResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopSupplierDetailResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopSupplierResponse;
import sk.tany.rest.api.dto.prestashop.PrestaShopSupplierWrapper;
import sk.tany.rest.api.dto.prestashop.PrestaShopSuppliersResponse;
import sk.tany.rest.api.exception.ImportException;
import sk.tany.rest.api.helper.StringHelper;
import sk.tany.rest.api.service.admin.BrandAdminService;
import sk.tany.rest.api.service.admin.CategoryAdminService;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.admin.ProductAdminService;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;
import sk.tany.rest.api.service.admin.SupplierAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrestaShopImportServiceImpl implements PrestaShopImportService {

    private final RestClient restClient;
    private final ProductAdminService productAdminService;
    private final SupplierAdminService supplierAdminService;
    private final BrandAdminService brandAdminService;
    private final CategoryAdminService categoryAdminService;
    private final ImageService imageService;
    private final ShopSettingsAdminService shopSettingsAdminService;
    private final sk.tany.rest.api.config.PrestaShopConfig prestaShopConfig;

    @Override
    public void importAllProducts() {
        log.info("Starting import of all products from PrestaShop");
        String url = String.format("%s/api/products?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), prestaShopConfig.getKey());
        try {
            PrestaShopProductsResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopProductsResponse.class);
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
            throw new ImportException("Error fetching product list from PrestaShop", e);
        }
        log.info("Finished import of all products from PrestaShop");
    }

    @Override
    public void importProduct(String id) {
        log.info("Importing product with ID: {}", id);
        String url = String.format("%s/api/products/%s?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), id, prestaShopConfig.getKey());
        try {
            PrestaShopProductWrapper wrapper = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopProductWrapper.class);
            if (wrapper != null && wrapper.getProduct() != null) {
                ProductAdminDto productDto = mapToProductDto(wrapper.getProduct());
                productAdminService.save(productDto);
                log.info("Successfully imported product with ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error importing product with ID: {}", id, e);
            throw new ImportException("Error importing product with ID: " + id, e);
        }
    }

    @Override
    public void importAllSuppliers() {
        log.info("Starting import of all suppliers from PrestaShop");
        String url = String.format("%s/api/suppliers?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), prestaShopConfig.getKey());
        try {
            PrestaShopSuppliersResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopSuppliersResponse.class);
            if (response != null && response.getSuppliers() != null) {
                for (PrestaShopSupplierResponse supplier : response.getSuppliers()) {
                    try {
                        importSupplier(String.valueOf(supplier.getId()));
                    } catch (Exception e) {
                        log.error("Failed to import supplier with ID: {}", supplier.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching supplier list from PrestaShop", e);
            throw new ImportException("Error fetching supplier list from PrestaShop", e);
        }
        log.info("Finished import of all suppliers from PrestaShop");
    }

    private void importSupplier(String id) {
        log.info("Importing supplier with ID: {}", id);
        String url = String.format("%s/api/suppliers/%s?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), id, prestaShopConfig.getKey());
        try {
            PrestaShopSupplierWrapper wrapper = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopSupplierWrapper.class);
            if (wrapper != null && wrapper.getSupplier() != null) {
                SupplierDto dto = mapToSupplierDto(wrapper.getSupplier());
                supplierAdminService.save(dto);
                log.info("Successfully imported supplier with ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error importing supplier with ID: {}", id, e);
            throw new ImportException("Error importing supplier with ID: " + id, e);
        }
    }

    private SupplierDto mapToSupplierDto(PrestaShopSupplierDetailResponse psSupplier) {
        SupplierDto dto = new SupplierDto();
        dto.setName(psSupplier.getName());
        dto.setPrestashopId(psSupplier.getId());
        dto.setMetaTitle(parseLanguageValue(psSupplier.getMetaTitle()));
        dto.setMetaDescription(parseLanguageValue(psSupplier.getMetaDescription()));
        return dto;
    }

    @Override
    public void importAllManufacturers() {
        log.info("Starting import of all manufacturers from PrestaShop");
        String url = String.format("%s/api/manufacturers?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), prestaShopConfig.getKey());
        try {
            PrestaShopManufacturersResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopManufacturersResponse.class);
            if (response != null && response.getManufacturers() != null) {
                for (PrestaShopManufacturerResponse manufacturer : response.getManufacturers()) {
                    try {
                        importManufacturer(String.valueOf(manufacturer.getId()));
                    } catch (Exception e) {
                        log.error("Failed to import manufacturer with ID: {}", manufacturer.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching manufacturer list from PrestaShop", e);
            throw new ImportException("Error fetching manufacturer list from PrestaShop", e);
        }
        log.info("Finished import of all manufacturers from PrestaShop");
    }

    private void importManufacturer(String id) {
        log.info("Importing manufacturer with ID: {}", id);
        String url = String.format("%s/api/manufacturers/%s?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), id, prestaShopConfig.getKey());
        try {
            PrestaShopManufacturerWrapper wrapper = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopManufacturerWrapper.class);
            if (wrapper != null && wrapper.getManufacturer() != null) {
                BrandDto dto = mapToBrandDto(wrapper.getManufacturer());
                brandAdminService.save(dto);
                log.info("Successfully imported manufacturer with ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error importing manufacturer with ID: {}", id, e);
            throw new ImportException("Error importing manufacturer with ID: " + id, e);
        }
    }

    private BrandDto mapToBrandDto(PrestaShopManufacturerDetailResponse psManufacturer) {
        BrandDto dto = new BrandDto();
        dto.setName(psManufacturer.getName());
        dto.setPrestashopId(psManufacturer.getId());
        dto.setMetaTitle(parseLanguageValue(psManufacturer.getMetaTitle()));
        dto.setMetaDescription(parseLanguageValue(psManufacturer.getMetaDescription()));
        dto.setSlug(parseLanguageValue(psManufacturer.getLinkRewrite()));
        dto.setActive("1".equals(psManufacturer.getActive()));
        String imageUrl = downloadAndUploadImage("manufacturers", psManufacturer.getId(), null, psManufacturer.getName(), ImageKitType.BRAND);
        dto.setImage(imageUrl);

        return dto;
    }

    private ProductAdminDto mapToProductDto(PrestaShopProductDetailResponse psProduct) {
        ShopSettingsGetResponse shopSettings = shopSettingsAdminService.get();
        BigDecimal vat = shopSettings.getVat();

        ProductAdminDto dto = new ProductAdminDto();
        dto.setProductIdentifier(psProduct.getId());
        dto.setTitle(parseLanguageValue(psProduct.getName()));
        dto.setDescription(parseLanguageValue(psProduct.getDescription()));
        dto.setShortDescription(parseLanguageValue(psProduct.getDescriptionShort()));
        dto.setPriceWithoutVat(psProduct.getPrice());
        dto.setPrice(psProduct.getPrice().multiply(BigDecimal.valueOf(1 + vat.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).doubleValue()), new MathContext(2, RoundingMode.HALF_UP)));
        dto.setWholesalePrice(psProduct.getWholesalePrice());
        dto.setWeight(psProduct.getWeight());
        dto.setProductCode(psProduct.getReference());
        dto.setEan(psProduct.getEan13());
        dto.setQuantity(10);

        dto.setStatus("1".equals(psProduct.getActive()) ? ProductStatus.AVAILABLE : ProductStatus.SOLD_OUT);

        List<String> prestashopCategoryIdList = getCategoryIds(psProduct);
        for (String prestashopCategoryId : prestashopCategoryIdList) {
            Optional<CategoryDto> categoryOptional = categoryAdminService.findByPrestashopId(Long.valueOf(prestashopCategoryId));
            categoryOptional.ifPresent(categoryDto -> dto.getCategoryIds().add(categoryDto.getId()));
        }

        Optional<SupplierDto> supplierOptional = supplierAdminService.findByPrestashopId(Long.valueOf(psProduct.getSupplierId()));
        supplierOptional.ifPresent(supplierDto -> dto.setSupplierId(supplierDto.getId()));

        Optional<BrandDto> brandOptional = brandAdminService.findByPrestashopId(Long.valueOf(psProduct.getManufacturerId()));
        brandOptional.ifPresent(brandDto -> dto.setBrandId(brandDto.getId()));

        List<String> imageUrls = new ArrayList<>();
        if (psProduct.getAssociations() != null && psProduct.getAssociations().getImages() != null) {
            for (PrestaShopImage psImage : psProduct.getAssociations().getImages()) {
                String imgUrl = downloadAndUploadImage("products", psProduct.getId(), psImage.getId(), dto.getTitle(), ImageKitType.PRODUCT);
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
                Object firstItem = list.getFirst();
                if (firstItem instanceof Map) {
                    return (String) ((Map<?, ?>) firstItem).get("value");
                }
            }
        }
        return "";
    }

    private String downloadAndUploadImage(String resource, Long id, String imageId, String name, ImageKitType type) {
        String urlPart = imageId != null ? id + "/" + imageId : String.valueOf(id);
        String imageUrl = String.format("%s/api/images/%s/%s?ws_key=%s", prestaShopConfig.getUrl(), resource, urlPart, prestaShopConfig.getKey());
        try {
            byte[] imageBytes = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);
            if (imageBytes != null && imageBytes.length > 0) {
                String filename = StringHelper.slugify(name) + ".jpg";
                return imageService.upload(imageBytes, filename, type);
            }
        } catch (Exception e) {
            log.error("Failed to download/upload image {} for {} {}", imageId, resource, id, e);
        }
        return null;
    }

    @Override
    public void importAllCategories() {
        log.info("Starting import of all categories from PrestaShop");
        String url = String.format("%s/api/categories?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), prestaShopConfig.getKey());
        try {
            PrestaShopCategoriesResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopCategoriesResponse.class);
            if (response != null && response.getCategories() != null) {
                for (PrestaShopCategoryResponse category : response.getCategories()) {
                    try {
                        importCategory(String.valueOf(category.getId()));
                    } catch (Exception e) {
                        log.error("Failed to import category with ID: {}", category.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching category list from PrestaShop", e);
            throw new ImportException("Error fetching category list from PrestaShop", e);
        }
        log.info("Finished import of all categories from PrestaShop");
    }

    @Override
    public void importCategory(String id) {
        log.info("Importing category with ID: {}", id);
        String url = String.format("%s/api/categories/%s?ws_key=%s&output_format=JSON", prestaShopConfig.getUrl(), id, prestaShopConfig.getKey());
        try {
            PrestaShopCategoryWrapper wrapper = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(PrestaShopCategoryWrapper.class);
            if (wrapper != null && wrapper.getCategory() != null) {
                CategoryDto dto = mapToCategoryDto(wrapper.getCategory());

                // Handle parent resolution
                String psParentId = wrapper.getCategory().getIdParent();
                if (psParentId != null && !"0".equals(psParentId)) { // "0" is usually the root in PrestaShop
                    Long parentIdLong = Long.valueOf(psParentId);
                    // Try to find parent locally
                    var parentOpt = categoryAdminService.findByPrestashopId(parentIdLong);
                    if (parentOpt.isEmpty()) {
                        // Recursively import parent if not found
                        log.info("Parent category {} not found for category {}, importing parent...", psParentId, id);
                        importCategory(psParentId);
                        parentOpt = categoryAdminService.findByPrestashopId(parentIdLong);
                    }
                    parentOpt.ifPresent(parent -> dto.setParentId(parent.getId()));
                }

                categoryAdminService.save(dto);
                log.info("Successfully imported category with ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error importing category with ID: {}", id, e);
            // Don't throw RuntimeException here to allow recursion to continue or fail gracefully
        }
    }

    private CategoryDto mapToCategoryDto(PrestaShopCategoryDetailResponse psCategory) {
        CategoryDto dto = new CategoryDto();
        dto.setPrestashopId(psCategory.getId());
        if (psCategory.getIdParent() != null) {
            dto.setPrestashopParentId(Long.valueOf(psCategory.getIdParent()));
        }
        dto.setTitle(parseLanguageValue(psCategory.getName()));
        dto.setDescription(parseLanguageValue(psCategory.getDescription()));
        dto.setMetaTitle(parseLanguageValue(psCategory.getMetaTitle()));
        dto.setMetaDescription(parseLanguageValue(psCategory.getMetaDescription()));
        dto.setSlug(parseLanguageValue(psCategory.getLinkRewrite()));
        dto.setActive("1".equals(psCategory.getActive()));
        dto.setVisible("1".equals(psCategory.getVisible()));
        dto.setPosition(Long.valueOf(psCategory.getPosition()));
        // parentId is handled in importCategory
        return dto;
    }
}
