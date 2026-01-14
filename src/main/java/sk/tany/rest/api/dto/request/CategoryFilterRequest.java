package sk.tany.rest.api.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CategoryFilterRequest {
    private List<FilterParameterRequest> filterParameters;
}
