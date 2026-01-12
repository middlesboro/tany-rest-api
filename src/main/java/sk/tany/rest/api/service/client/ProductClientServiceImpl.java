package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.mapper.ProductMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductClientServiceImpl implements ProductClientService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSearchEngine productSearchEngine;

    @Override
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        return productRepository.findById(id).map(productMapper::toDto);
    }

    @Override
    public Page<ProductDto> search(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIds(categoryId, pageable).map(productMapper::toDto);
    }

    @Override
    public java.util.List<ProductDto> findAllByIds(Iterable<String> ids) {
        return productRepository.findAllById(ids).stream().map(productMapper::toDto).toList();
    }

    @Override
    public java.util.List<ProductDto> searchProducts(String query) {
        return productSearchEngine.searchAndSort(query)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public void updateProductStock(String productId, Integer quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
        int newQuantity = currentQuantity - quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Not enough stock for product: " + product.getTitle());
        }
        product.setQuantity(newQuantity);
        productRepository.save(product);
        productSearchEngine.updateProduct(product);
    }
}
