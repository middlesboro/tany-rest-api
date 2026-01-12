package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryClientService {
    List<CategoryDto> findAllVisible();
    Optional<CategoryDto> findById(String id);
}
