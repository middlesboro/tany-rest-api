package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.component.SlugGenerator;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.isklad.UpdateInventoryCardRequest;
import sk.tany.rest.api.mapper.ProductMapper;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.SequenceService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAdminServiceImpl implements ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSearchEngine productSearchEngine;
    private final ImageService imageService;
    private final ReviewRepository reviewRepository;
    private final SlugGenerator slugGenerator;
    private final SequenceService sequenceService;
    private final ISkladService iskladService;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public Page<ProductAdminDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toAdminDto);
    }

    @Override
    public Page<ProductAdminDto> findAll(ProductFilter filter, Pageable pageable) {
        return productSearchEngine.search(filter, pageable).map(productMapper::toAdminDto);
    }

    @Override
    public Optional<ProductAdminDto> findById(String id) {
        return productRepository.findById(id).map(productMapper::toAdminDto);
    }

    @Override
    public ProductAdminDto save(ProductAdminDto productDto) {
        var product = productMapper.toEntity(productDto);
        if (product.getProductIdentifier() == null) {
            product.setProductIdentifier(sequenceService.getNextSequence("product_identifier"));
        } else {
            sequenceService.ensureSequenceAtLeast("product_identifier", product.getProductIdentifier());
        }
        recalculateReviewStatistics(product);
        calculateProductPrices(product);
        if (StringUtils.isBlank(product.getSlug())) {
            product.setSlug(slugGenerator.generateSlug(product.getTitle(), null));
        }
        var savedProduct = productRepository.save(product);
        productSearchEngine.addProduct(savedProduct);
        sendToIsklad(savedProduct);
        return productMapper.toAdminDto(savedProduct);
    }

    @Override
    public ProductAdminDto update(String id, ProductAdminDto productDto) {
        productDto.setId(id);
        var product = productMapper.toEntity(productDto);
        recalculateReviewStatistics(product);
        calculateProductPrices(product);
        if (StringUtils.isBlank(product.getSlug())) {
            product.setSlug(slugGenerator.generateSlug(product.getTitle(), id));
        }
        var savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);
        sendToIsklad(savedProduct);
        return productMapper.toAdminDto(savedProduct);
    }

    @Override
    public ProductAdminDto patch(String id, sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest patchDto) {
        var product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        recalculateReviewStatistics(product);

        if (patchDto.getDiscountValue() != null) {
            if (patchDto.getDiscountPrice() == null) {
                product.setDiscountPrice(null);
            }
        } else if (patchDto.getDiscountPrice() != null) {
            product.setDiscountValue(null);
        }

        productMapper.updateEntityFromPatch(patchDto, product);
        calculateProductPrices(product);
        var savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);
        return productMapper.toAdminDto(savedProduct);
    }

    @Override
    public void deleteById(String id) {
        var product = productRepository.findById(id);
        if (product.isPresent()) {
            var images = product.get().getImages();
            if (images != null) {
                images.forEach(imageService::delete);
            }
            productRepository.deleteById(id);
            productSearchEngine.removeProduct(id);
        }
    }

    @Override
    public Page<ProductAdminDto> search(String categoryId, Pageable pageable) {
        return productSearchEngine.findByCategoryIds(categoryId, pageable, false).map(productMapper::toAdminDto);
    }

    @Override
    public java.util.List<ProductAdminDto> findAllByIds(Iterable<String> ids) {
        return productRepository.findAllById(ids).stream().map(productMapper::toAdminDto).toList();
    }

    @Override
    public java.util.List<ProductAdminDto> searchByQuery(String query) {
        return productSearchEngine.searchAndSort(query, false).stream()
                .map(productMapper::toAdminDto)
                .toList();
    }

    @Override
    public java.util.List<ProductAdminDto> findAllByFilterParameterValueId(String filterParameterValueId) {
        return productRepository.findAllByProductFilterParametersFilterParameterValueId(filterParameterValueId).stream()
                .map(productMapper::toAdminDto)
                .toList();
    }

    @Override
    public void generateMissingSlugs() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (StringUtils.isBlank(product.getSlug())) {
                product.setSlug(slugGenerator.generateSlug(product.getTitle(), product.getId()));
                productRepository.save(product);
                productSearchEngine.updateProduct(product);
            }
        }
    }

    @Override
    public void updateAllProductsQuantity(Integer quantity) {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            product.setQuantity(quantity);
            var savedProduct = productRepository.save(product);
            productSearchEngine.updateProduct(savedProduct);
        }
    }

    private void calculateProductPrices(Product product) {
        BigDecimal price = product.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            product.setDiscountValue(null);
            product.setDiscountPrice(null);
            product.setDiscountPercentualValue(null);
            product.setDiscountPriceWithoutVat(null);
            return;
        }

        // todo take vat from shop settings
        product.setPriceWithoutVat(product.getPrice().divide(new BigDecimal("1.23"), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP));

        if (product.getDiscountValue() != null) {
            BigDecimal discountPrice = price.subtract(product.getDiscountValue());
            if (discountPrice.compareTo(BigDecimal.ZERO) < 0) discountPrice = BigDecimal.ZERO;
            product.setDiscountPrice(discountPrice);
        } else if (product.getDiscountPrice() != null) {
            BigDecimal discountValue = price.subtract(product.getDiscountPrice());
            if (discountValue.compareTo(BigDecimal.ZERO) < 0) discountValue = BigDecimal.ZERO;
            product.setDiscountValue(discountValue);
        }

        if (product.getDiscountValue() != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percent = product.getDiscountValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(price, 2, RoundingMode.HALF_UP);
            product.setDiscountPercentualValue(percent);
        } else {
            product.setDiscountPercentualValue(null);
        }

        if (product.getDiscountPrice() != null && product.getPriceWithoutVat() != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountPriceWithoutVat = product.getDiscountPrice()
                    .multiply(product.getPriceWithoutVat())
                    .divide(price, 2, RoundingMode.HALF_UP);
            product.setDiscountPriceWithoutVat(discountPriceWithoutVat);
        } else {
            product.setDiscountPriceWithoutVat(null);
        }
    }

    private void recalculateReviewStatistics(Product product) {
        if (product.getId() == null) {
            product.setAverageRating(BigDecimal.ZERO);
            product.setReviewsCount(0);
            return;
        }

        List<Review> reviews = reviewRepository.findAllByProductId(product.getId());
        List<Review> activeReviews = reviews.stream()
                .filter(Review::isActive)
                .toList();

        int count = activeReviews.size();
        if (count == 0) {
            product.setAverageRating(BigDecimal.ZERO);
            product.setReviewsCount(0);
            return;
        }

        double average = activeReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setAverageRating(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
        product.setReviewsCount(count);
    }

    private void sendToIsklad(Product product) {
        try {
            String brandName = null;
            if (StringUtils.isNotBlank(product.getBrandId())) {
                brandName = brandRepository.findById(product.getBrandId())
                        .map(sk.tany.rest.api.domain.brand.Brand::getName)
                        .orElse(null);
            }

            String supplierName = null;
            if (StringUtils.isNotBlank(product.getSupplierId())) {
                supplierName = supplierRepository.findById(product.getSupplierId())
                        .map(sk.tany.rest.api.domain.supplier.Supplier::getName)
                        .orElse(null);
            }

            List<String> images = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                images = Collections.singletonList(product.getImages().get(0));
            }

            var request = UpdateInventoryCardRequest.builder()
                    .itemId(product.getProductIdentifier())
                    .name(product.getTitle())
                    .ean(product.getEan())
                    .priceWithoutTax(product.getPriceWithoutVat())
                    .mj("ks")
                    .enabled(product.isActive())
                    .producer(brandName)
                    .supplier(supplierName)
                    .images(images)
                    .build();
            iskladService.createOrUpdateProduct(request);
        } catch (Exception e) {
            log.error("Failed to send product to iSklad: {}", product.getId(), e);
        }
    }
}
