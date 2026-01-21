package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import sk.tany.rest.api.service.admin.FilterParameterValueAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.admin.filterparametervalue.patch.FilterParameterValuePatchRequest;
import sk.tany.rest.api.mapper.FilterParameterValueMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilterParameterValueAdminServiceImpl implements FilterParameterValueAdminService {

    private final FilterParameterValueRepository repository;
    private final FilterParameterRepository filterParameterRepository;
    private final FilterParameterValueMapper mapper;

    @Override
    public FilterParameterValueDto save(FilterParameterValueDto dto) {
        FilterParameterValue entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Page<FilterParameterValueDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<FilterParameterValueDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public FilterParameterValueDto update(String id, FilterParameterValueDto dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDto(dto, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FilterParameterValue not found with id: " + id));
    }

    @Override
    public FilterParameterValueDto patch(String id, FilterParameterValuePatchRequest patch) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromPatch(patch, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FilterParameterValue not found with id: " + id));
    }

    @Override

    public void deleteById(String id) {
        // Remove this value ID from all FilterParameters that contain it
        List<FilterParameter> parameters = filterParameterRepository.findAllByFilterParameterValueIdsContaining(id);
        for (FilterParameter param : parameters) {
            param.getFilterParameterValueIds().remove(id);
            filterParameterRepository.save(param);
        }

        repository.deleteById(id);
    }
}
