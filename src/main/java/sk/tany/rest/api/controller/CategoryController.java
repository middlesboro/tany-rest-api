package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.service.client.CategoryClientService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryClientService categoryService;

    @GetMapping
    public java.util.List<CategoryDto> getCategories() {
        return categoryService.findAll();
    }

}
