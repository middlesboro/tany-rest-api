package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.service.client.CategoryClientService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryClientController {

    private final CategoryClientService categoryService;

    @PostMapping
    public sk.tany.rest.api.dto.response.CategoryClientResponse getCategories(@RequestBody(required = false) CategoryFilterRequest request) {
        return categoryService.getCategoryData(request);
    }

}
