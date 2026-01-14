package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import sk.tany.rest.api.service.admin.FilterParameterAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.admin.filterparameter.patch.FilterParameterPatchRequest;
import sk.tany.rest.api.mapper.FilterParameterMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilterParameterAdminServiceImpl implements FilterParameterAdminService {

    private final FilterParameterRepository repository;
    private final FilterParameterMapper mapper;

    @Override
    public FilterParameterDto save(FilterParameterDto dto) {
        FilterParameter entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Page<FilterParameterDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<FilterParameterDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public FilterParameterDto update(String id, FilterParameterDto dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDto(dto, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FilterParameter not found with id: " + id));
    }

    @Override
    public FilterParameterDto patch(String id, FilterParameterPatchRequest patch) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromPatch(patch, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FilterParameter not found with id: " + id));
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
