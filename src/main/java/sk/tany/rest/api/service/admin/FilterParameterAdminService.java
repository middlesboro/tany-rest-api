package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.admin.filterparameter.patch.FilterParameterPatchRequest;

import java.util.Optional;

public interface FilterParameterAdminService {
    FilterParameterDto save(FilterParameterDto dto);
    Page<FilterParameterDto> findAll(Pageable pageable);
    Optional<FilterParameterDto> findById(String id);
    FilterParameterDto update(String id, FilterParameterDto dto);
    FilterParameterDto patch(String id, FilterParameterPatchRequest patch);
    void deleteById(String id);
}
