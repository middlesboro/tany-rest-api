package sk.tany.rest.api.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class FilterParameterRequest {
    private String id;
    private List<String> filterParameterValueIds;
}
