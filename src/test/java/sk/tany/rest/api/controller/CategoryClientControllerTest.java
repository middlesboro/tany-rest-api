package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.controller.client.CategoryClientController;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.service.client.CategoryClientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        when(categoryService.findAll()).thenReturn(categoryList);

        List<CategoryDto> result = categoryClientController.getCategories();

        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getTitle());
        verify(categoryService, times(1)).findAll();
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

        when(categoryService.findAll()).thenReturn(tree);

        List<CategoryDto> result = categoryClientController.getCategories();

        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getTitle());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("Child", result.get(0).getChildren().get(0).getTitle());

        verify(categoryService, times(1)).findAll();
    }
}
