package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.service.client.CategoryClientService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @Mock
    private CategoryClientService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCategories_ShouldReturnListOfCategories() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("Test Category");
        List<CategoryDto> categoryList = Collections.singletonList(categoryDto);

        when(categoryService.findAll()).thenReturn(categoryList);

        List<CategoryDto> result = categoryController.getCategories();

        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getTitle());
        verify(categoryService, times(1)).findAll();
    }
}
