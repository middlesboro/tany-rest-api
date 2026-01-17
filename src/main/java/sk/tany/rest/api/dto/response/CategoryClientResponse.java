package sk.tany.rest.api.dto.response;

import lombok.Data;
import sk.tany.rest.api.dto.CategoryDto;
import sk.tany.rest.api.dto.ClientFilterParameterDto;

import java.util.List;

@Data
public class CategoryClientResponse {
    private List<CategoryDto> categories;
    private List<ClientFilterParameterDto> filterParameters;
}
