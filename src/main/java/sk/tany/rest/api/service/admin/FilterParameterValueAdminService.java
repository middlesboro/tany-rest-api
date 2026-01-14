package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.admin.filterparametervalue.patch.FilterParameterValuePatchRequest;

import java.util.Optional;

public interface FilterParameterValueAdminService {
    FilterParameterValueDto save(FilterParameterValueDto dto);
    Page<FilterParameterValueDto> findAll(Pageable pageable);
    Optional<FilterParameterValueDto> findById(String id);
    FilterParameterValueDto update(String id, FilterParameterValueDto dto);
    FilterParameterValueDto patch(String id, FilterParameterValuePatchRequest patch);
    void deleteById(String id);
}
