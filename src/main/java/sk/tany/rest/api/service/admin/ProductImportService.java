package sk.tany.rest.api.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterType;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductFilterParameter;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productlabel.ProductLabel;
import sk.tany.rest.api.domain.productlabel.ProductLabelPosition;
import sk.tany.rest.api.domain.productlabel.ProductLabelRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.admin.import_product.ProductImportDataDto;
import sk.tany.rest.api.dto.admin.import_product.ProductImportEntryDto;
import sk.tany.rest.api.exception.ImportException;
import sk.tany.rest.api.component.SlugGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportService {

    private final ProductRepository productRepository;
    private final ProductSalesRepository productSalesRepository;
    private final ProductLabelRepository productLabelRepository;
    private final FilterParameterRepository filterParameterRepository;
    private final FilterParameterValueRepository filterParameterValueRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final BrandRepository brandRepository;
    private final ObjectMapper objectMapper;
    private final ProductSearchEngine productSearchEngine;
    private final SlugGenerator slugGenerator;


    public void importProducts() {
        try {
            ClassPathResource resource = new ClassPathResource("products.json");
            InputStream inputStream = resource.getInputStream();
            List<ProductImportEntryDto> entries = objectMapper.readValue(inputStream, new TypeReference<List<ProductImportEntryDto>>() {});

            Optional<ProductImportEntryDto> tableEntry = entries.stream()
                    .filter(e -> "table".equals(e.getType()) && "p_sale".equals(e.getName()))
                    .findFirst();

            if (tableEntry.isPresent()) {
                List<ProductImportDataDto> data = tableEntry.get().getData();
                if (data == null || data.isEmpty()) {
                    log.warn("No data found in products.json");
                    return;
                }

                Map<String, List<ProductImportDataDto>> productsMap = data.stream()
                        .filter(d -> StringUtils.isNotBlank(d.getIdProduct()))
                        .collect(Collectors.groupingBy(ProductImportDataDto::getIdProduct));

                for (Map.Entry<String, List<ProductImportDataDto>> entry : productsMap.entrySet()) {
                    processProduct(entry.getKey(), entry.getValue());
                }
            } else {
                log.warn("Table p_label_p not found in products.json");
            }

        } catch (IOException e) {
            log.error("Error importing products", e);
            throw new ImportException("Error importing products", e);
        }
    }

    private void processProduct(String idProductStr, List<ProductImportDataDto> rows) {
        if (rows.isEmpty()) return;

        ProductImportDataDto baseData = rows.get(0);
        Long prestashopId = Long.parseLong(idProductStr);

        Product product = productRepository.findByPrestashopId(prestashopId).orElse(new Product());
        product.setPrestashopId(prestashopId);
        product.setTitle(baseData.getProductName());
        product.setProductCode(baseData.getProductCode());
        product.setEan(baseData.getEan());
        product.setDescription(baseData.getFullDescription());
        product.setShortDescription(baseData.getShortDescription());

        if (StringUtils.isNotBlank(baseData.getWeight())) {
            product.setWeight(new BigDecimal(baseData.getWeight()));
        }
        if (StringUtils.isNotBlank(baseData.getPriceTaxExcl())) {
            product.setPriceWithoutVat(new BigDecimal(baseData.getPriceTaxExcl()));
            product.setPrice(product.getPriceWithoutVat().multiply(new BigDecimal("1.23")).setScale(2, RoundingMode.HALF_UP));
        }
        if (StringUtils.isNotBlank(baseData.getWholesalePrice())) {
            product.setWholesalePrice(new BigDecimal(baseData.getWholesalePrice()));
        }
        if (StringUtils.isNotBlank(baseData.getStockQty())) {
            product.setQuantity(Integer.parseInt(baseData.getStockQty()));
        }

        // Process Supplier
        String supplierName = baseData.getSupplierName();
        if (StringUtils.isNotBlank(supplierName)) {
            Supplier supplier = supplierRepository.findByName(supplierName)
                    .orElseGet(() -> {
                        Supplier newSupplier = new Supplier();
                        newSupplier.setName(supplierName);
                        return supplierRepository.save(newSupplier);
                    });
            product.setSupplierId(supplier.getId());
        }

        // Process Brand
        String brandName = baseData.getBrandName();
        if (StringUtils.isNotBlank(brandName)) {
            Brand brand = brandRepository.findByName(brandName)
                    .orElseGet(() -> {
                        Brand newBrand = new Brand();
                        newBrand.setName(brandName);
                        newBrand.setActive(true);
                        return brandRepository.save(newBrand);
                    });
            product.setBrandId(brand.getId());
        }

        // Process Categories
        Set<String> categoryIds = new HashSet<>();
        rows.stream()
                .map(ProductImportDataDto::getCategoryId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .forEach(catIdStr -> {
                    try {
                        Long catPrestaId = Long.parseLong(catIdStr);
                        categoryRepository.findByPrestashopId(catPrestaId)
                                .ifPresent(cat -> categoryIds.add(cat.getId()));
                    } catch (NumberFormatException ignored) {}
                });
        product.setCategoryIds(new ArrayList<>(categoryIds));

        // Process Images
        List<ImageInfo> images = rows.stream()
                .filter(r -> StringUtils.isNotBlank(r.getImageUrl()))
                .map(r -> new ImageInfo(r.getImageUrl(), "1".equals(r.getIsCover())))
                .distinct() // Need equals/hashcode on ImageInfo
                .sorted(Comparator.comparing(ImageInfo::isCover).reversed())
                .collect(Collectors.toList());

        // todo import image via imageservice to imakegit and get proper url and save it to product.
//         Deduplicate by URL (keeping the one with isCover if multiple entries for same URL exist)
        List<String> finalImages = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>();
        for (ImageInfo img : images) {
             if (processedUrls.add(img.url)) {
                 finalImages.add(img.url);
             }
        }
        product.setImages(finalImages);


        // Process Labels
        Set<String> labelIds = new HashSet<>();
        rows.stream()
                .filter(r -> StringUtils.isNotBlank(r.getLabelText()))
                .forEach(r -> {
                    String labelText = r.getLabelText();
                    ProductLabel label = productLabelRepository.findByTitle(labelText)
                            .orElseGet(() -> {
                                ProductLabel newLabel = new ProductLabel();
                                newLabel.setTitle(labelText);
                                newLabel.setColor(r.getLabelColor());
                                newLabel.setBackgroundColor(r.getLabelBackgroundColor());
                                newLabel.setActive("1".equals(r.getLabelStatus()));
                                newLabel.setPosition(ProductLabelPosition.TOP_RIGHT); // Default
                                return productLabelRepository.save(newLabel);
                            });
                    labelIds.add(label.getId());
                });
        product.setProductLabelIds(new ArrayList<>(labelIds));

        // Process Filters
        List<ProductFilterParameter> productFilters = new ArrayList<>();
        Set<String> processedFilterPairs = new HashSet<>();

        for (ProductImportDataDto row : rows) {
            String paramName = row.getFilterParameter();
            String valueName = row.getFilterParameterValue();

            if (StringUtils.isNotBlank(paramName) && StringUtils.isNotBlank(valueName)) {
                String pairKey = paramName + "::" + valueName;
                if (processedFilterPairs.contains(pairKey)) continue;
                processedFilterPairs.add(pairKey);

                FilterParameter filterParam = filterParameterRepository.findByName(paramName)
                        .orElseGet(() -> {
                            FilterParameter newParam = new FilterParameter();
                            newParam.setName(paramName);
                            newParam.setType(FilterParameterType.TAG);
                            newParam.setActive(true);
                            newParam.setFilterParameterValueIds(new ArrayList<>());
                            FilterParameter saved = filterParameterRepository.save(newParam);
                            productSearchEngine.addFilterParameter(saved);
                            return saved;
                        });

                FilterParameterValue filterValue = filterParameterValueRepository.findByNameAndFilterParameterId(valueName, filterParam.getId())
                        .orElseGet(() -> {
                            FilterParameterValue newValue = new FilterParameterValue();
                            newValue.setName(valueName);
                            newValue.setFilterParameterId(filterParam.getId());
                            newValue.setActive(true);
                            FilterParameterValue savedValue = filterParameterValueRepository.save(newValue);
                            productSearchEngine.addFilterParameterValue(savedValue);

                            // Add to parent param list
                            if (filterParam.getFilterParameterValueIds() == null) {
                                filterParam.setFilterParameterValueIds(new ArrayList<>());
                            }
                            filterParam.getFilterParameterValueIds().add(savedValue.getId());
                            FilterParameter updatedParam = filterParameterRepository.save(filterParam);
                            productSearchEngine.addFilterParameter(updatedParam);

                            return savedValue;
                        });

                ProductFilterParameter pfp = new ProductFilterParameter();
                pfp.setFilterParameterId(filterParam.getId());
                pfp.setFilterParameterValueId(filterValue.getId());
                productFilters.add(pfp);
            }
        }
        product.setProductFilterParameters(productFilters);

        if (StringUtils.isBlank(product.getSlug())) {
            product.setSlug(slugGenerator.generateSlug(product.getTitle(), product.getId()));
        }

        Product savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);

        if (baseData.getSoldQuantity() != null) {
            int soldQty = baseData.getSoldQuantity();
            ProductSales ps = new ProductSales();
            ps.setProductId(savedProduct.getId());
            ps.setSalesCount(soldQty);
            ProductSales savedProductSales = productSalesRepository.save(ps);
            productSearchEngine.updateSalesCount(savedProductSales.getProductId(), savedProductSales.getSalesCount());
        }
    }

    private static class ImageInfo {
        String url;
        boolean isCover;

        public ImageInfo(String url, boolean isCover) {
            this.url = url;
            this.isCover = isCover;
        }

        public boolean isCover() {
            return isCover;
        }

        // Simple distinct logic helper
        @Override
        public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             ImageInfo that = (ImageInfo) o;
             return isCover == that.isCover && java.util.Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(url, isCover);
        }
    }
}
