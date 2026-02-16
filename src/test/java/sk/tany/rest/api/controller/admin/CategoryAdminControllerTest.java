package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.service.admin.CategoryAdminService;
import sk.tany.rest.api.service.admin.PrestaShopImportService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryAdminControllerTest {

    @Mock
    private CategoryAdminService categoryService;

    @Mock
    private PrestaShopImportService prestaShopImportService;

    @InjectMocks
    private CategoryAdminController categoryAdminController;

    @Test
    void addFilterParameters() {
        String id = "cat1";
        List<String> filterIds = List.of("fp1", "fp2");
        CategoryDto expectedDto = new CategoryDto();
        expectedDto.setId(id);

        when(categoryService.addFilterParameters(id, filterIds)).thenReturn(expectedDto);

        ResponseEntity<CategoryDto> response = categoryAdminController.addFilterParameters(id, filterIds);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
    }

    @Test
    void getCategories_ShouldReturnPagedCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("Test Category");
        Page<CategoryDto> categoryPage = new PageImpl<>(Collections.singletonList(categoryDto));

        when(categoryService.findAll(pageable)).thenReturn(categoryPage);

        Page<CategoryDto> result = categoryAdminController.getCategories(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Category", result.getContent().getFirst().getTitle());
        verify(categoryService, times(1)).findAll(pageable);
    }

    @Test
    void getCategories_WithQuery_ShouldReturnFilteredCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        String query = "Test";
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("Test Category");
        Page<CategoryDto> categoryPage = new PageImpl<>(Collections.singletonList(categoryDto));

        when(categoryService.findAll(query, pageable)).thenReturn(categoryPage);

        Page<CategoryDto> result = categoryAdminController.getCategories(query, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Category", result.getContent().getFirst().getTitle());
        verify(categoryService, times(1)).findAll(query, pageable);
    }
}
