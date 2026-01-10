package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.mapper.ProductMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductAdminServiceImpl implements ProductAdminService {

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
    public ProductDto save(ProductDto productDto) {
        var product = productMapper.toEntity(productDto);
        var savedProduct = productRepository.save(product);
        productSearchEngine.addProduct(savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto update(String id, ProductDto productDto) {
        productDto.setId(id);
        var product = productMapper.toEntity(productDto);
        var savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto patch(String id, sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest patchDto) {
        var product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productMapper.updateEntityFromPatch(patchDto, product);
        var savedProduct = productRepository.save(product);
        productSearchEngine.updateProduct(savedProduct);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public void deleteById(String id) {
        productRepository.deleteById(id);
        productSearchEngine.removeProduct(id);
    }

    @Override
    public Page<ProductDto> search(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIds(categoryId, pageable).map(productMapper::toDto);
    }

    @Override
    public java.util.List<ProductDto> findAllByIds(Iterable<String> ids) {
        return productRepository.findAllById(ids).stream().map(productMapper::toDto).toList();
    }
}
