package sk.tany.rest.api.service.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.response.CategoryClientResponse;
import sk.tany.rest.api.mapper.CategoryMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryClientServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ProductSearchEngine productSearchEngine;

    @InjectMocks
    private CategoryClientServiceImpl categoryClientService;

    @Test
    void getCategoryData_shouldReturnResponse() {
        // Arrange
        CategoryFilterRequest request = new CategoryFilterRequest();
        when(productSearchEngine.filterProducts(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(productSearchEngine.getClientFilterParameters(any(), any(), any())).thenReturn(new ArrayList<>());

        // Act
        CategoryClientResponse response = categoryClientService.getCategoryData(request);

        // Assert
        assertNotNull(response);
        verify(productSearchEngine, times(1)).filterProducts(any());
        // Then `relevantCategoryIds` is empty.
        // `finalCategoryIdsToKeep` populated from all categories if request empty.
        // `filteredCategories` might be empty if allCategories empty.
        // If `filteredCategories` empty, `baseProducts` empty.
        // `getClientFilterParameters` called.
        // `productSearchEngine.filterProducts(null)` is called inside the `else` block of `if (filteredCategories.isEmpty())`.
        // If repository returns empty, filteredCategories is empty. So `filterProducts(null)` NOT called.
        // So `filterProducts` called once.
        verify(productSearchEngine, times(1)).filterProducts(request);
        verify(categoryRepository).findAll();
    }
}
