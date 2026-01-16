package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.controller.client.CategoryClientController;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.response.CategoryClientResponse;
import sk.tany.rest.api.service.client.CategoryClientService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryClientControllerTest {

    @Mock
    private CategoryClientService categoryService;

    @InjectMocks
    private CategoryClientController categoryClientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCategories_ShouldReturnListOfCategories() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setTitle("Test Category");
        List<CategoryDto> categoryList = Collections.singletonList(categoryDto);

        CategoryClientResponse response = new CategoryClientResponse();
        response.setCategories(categoryList);

        when(categoryService.getCategoryData(any())).thenReturn(response);

        CategoryClientResponse result = categoryClientController.getCategories(null);

        assertEquals(1, result.getCategories().size());
        assertEquals("Test Category", result.getCategories().get(0).getTitle());
        verify(categoryService, times(1)).getCategoryData(any());
    }

    @Test
    void getCategories_ShouldReturnTreeStructure() {
        // Create root category
        CategoryDto root = new CategoryDto();
        root.setId("1");
        root.setTitle("Root");
        root.setParentId(null);
        root.setChildren(new ArrayList<>());

        // Create child category
        CategoryDto child = new CategoryDto();
        child.setId("2");
        child.setTitle("Child");
        child.setParentId("1");
        child.setChildren(new ArrayList<>());

        // Add child to root
        root.getChildren().add(child);

        // Service should return only root
        List<CategoryDto> tree = Collections.singletonList(root);

        CategoryClientResponse response = new CategoryClientResponse();
        response.setCategories(tree);

        when(categoryService.getCategoryData(any())).thenReturn(response);

        CategoryClientResponse result = categoryClientController.getCategories(null);

        assertEquals(1, result.getCategories().size());
        assertEquals("Root", result.getCategories().get(0).getTitle());
        assertEquals(1, result.getCategories().get(0).getChildren().size());
        assertEquals("Child", result.getCategories().get(0).getChildren().get(0).getTitle());

        verify(categoryService, times(1)).getCategoryData(any());
    }
}
