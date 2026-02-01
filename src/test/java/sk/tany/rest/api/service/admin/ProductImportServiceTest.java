package sk.tany.rest.api.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.productlabel.ProductLabel;
import sk.tany.rest.api.domain.productlabel.ProductLabelRepository;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.admin.import_product.ProductImportDataDto;
import sk.tany.rest.api.dto.admin.import_product.ProductImportEntryDto;
import sk.tany.rest.api.service.common.SequenceService;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImportServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductLabelRepository productLabelRepository;
    @Mock
    private FilterParameterRepository filterParameterRepository;
    @Mock
    private FilterParameterValueRepository filterParameterValueRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ProductSearchEngine productSearchEngine;
    @Mock
    private sk.tany.rest.api.component.SlugGenerator slugGenerator;
    @Mock
    private SequenceService sequenceService;

    @InjectMocks
    private ProductImportService productImportService;

    @Test
    void importProducts_shouldProcessData() throws Exception {
        // Arrange
        ProductImportEntryDto entry = new ProductImportEntryDto();
        entry.setType("table");
        entry.setName("p_sale");

        ProductImportDataDto data = new ProductImportDataDto();
        data.setIdProduct("123");
        data.setProductName("Test Product");
        data.setProductCode("CODE123");
        data.setSupplierName("Supplier1");
        data.setBrandName("Brand1");
        data.setCategoryId("10");
        data.setLabelText("NewLabel");
        data.setFilterParameter("Color");
        data.setFilterParameterValue("Red");
        data.setImageUrl("http://image.com/1.jpg");
        data.setIsCover("1");
        data.setExternalStock("0");
        data.setIsDefaultCategory("1");

        entry.setData(List.of(data));
        List<ProductImportEntryDto> entries = List.of(entry);

        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(entries);

        when(productRepository.findByPrestashopId(123L)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId("p1");
            return p;
        });

        when(supplierRepository.findByName("Supplier1")).thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(i -> {
            Supplier s = i.getArgument(0);
            s.setId("s1");
            return s;
        });
        when(brandRepository.findByName("Brand1")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(i -> {
            Brand b = i.getArgument(0);
            b.setId("b1");
            return b;
        });
        when(categoryRepository.findByPrestashopId(10L)).thenReturn(Optional.of(new Category()));
        when(productLabelRepository.findByTitle("NewLabel")).thenReturn(Optional.empty());
        when(productLabelRepository.save(any(ProductLabel.class))).thenAnswer(i -> {
            ProductLabel l = i.getArgument(0);
            l.setId("l1");
            return l;
        });
        when(filterParameterRepository.findByName("Color")).thenReturn(Optional.empty());
        when(filterParameterRepository.save(any(FilterParameter.class))).thenAnswer(i -> {
            FilterParameter f = i.getArgument(0);
            f.setId("fp1");
            return f;
        });
        when(filterParameterValueRepository.findByNameAndFilterParameterId("Red", "fp1")).thenReturn(Optional.empty());
        when(filterParameterValueRepository.save(any(FilterParameterValue.class))).thenAnswer(i -> {
            FilterParameterValue v = i.getArgument(0);
            v.setId("fpv1");
            return v;
        });

        // Act
        productImportService.importProducts();

        // Assert
        verify(productRepository).save(any(Product.class));
        verify(supplierRepository).save(any(Supplier.class));
        verify(brandRepository).save(any(Brand.class));
        verify(productLabelRepository).save(any(ProductLabel.class));
        // It can be saved multiple times (creation + adding value)
        verify(filterParameterRepository, org.mockito.Mockito.atLeastOnce()).save(any(FilterParameter.class));
        verify(filterParameterValueRepository).save(any(FilterParameterValue.class));

        verify(productSearchEngine).updateProduct(any(Product.class));
        verify(productSearchEngine, org.mockito.Mockito.atLeastOnce()).addFilterParameter(any(FilterParameter.class));
        verify(productSearchEngine).addFilterParameterValue(any(FilterParameterValue.class));
    }
}
