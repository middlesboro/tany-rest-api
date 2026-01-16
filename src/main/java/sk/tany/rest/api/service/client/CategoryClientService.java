package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;

import java.util.List;
import java.util.Optional;

public interface CategoryClientService {
    List<CategoryDto> findAllVisible();
    Optional<CategoryDto> findById(String id);
    List<FilterParameterDto> getFilterParameters(String categoryId, CategoryFilterRequest request);
    sk.tany.rest.api.dto.response.CategoryClientResponse getCategoryData(CategoryFilterRequest request);
}
