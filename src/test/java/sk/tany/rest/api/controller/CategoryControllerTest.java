package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.service.CategoryService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCategories_ShouldReturnPagedCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("Test Category");
        Page<CategoryDto> categoryPage = new PageImpl<>(Collections.singletonList(categoryDto));

        when(categoryService.findAll(pageable)).thenReturn(categoryPage);

        Page<CategoryDto> result = categoryController.getCategories(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Category", result.getContent().get(0).getTitle());
        verify(categoryService, times(1)).findAll(pageable);
    }
}
