package sk.tany.rest.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.mapper.ProductMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findAll_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        ProductDto productDto = new ProductDto();

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        Page<ProductDto> result = productService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
        verify(productMapper, times(1)).toDto(any(Product.class));
    }
}
