package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.customeremail.CustomerEmail;
import sk.tany.rest.api.domain.customeremail.CustomerEmailRepository;
import sk.tany.rest.api.dto.admin.customeremail.CustomerEmailDto;
import sk.tany.rest.api.dto.admin.customeremail.patch.CustomerEmailPatchRequest;
import sk.tany.rest.api.mapper.CustomerEmailMapper;
import sk.tany.rest.api.service.admin.CustomerEmailAdminService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerEmailAdminServiceImpl implements CustomerEmailAdminService {

    private final CustomerEmailRepository repository;
    private final CustomerEmailMapper mapper;

    @Override
    public CustomerEmailDto save(CustomerEmailDto dto) {
        CustomerEmail entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Page<CustomerEmailDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<CustomerEmailDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public CustomerEmailDto update(String id, CustomerEmailDto dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDto(dto, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CustomerEmail not found with id: " + id));
    }

    @Override
    public CustomerEmailDto patch(String id, CustomerEmailPatchRequest patch) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromPatch(patch, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CustomerEmail not found with id: " + id));
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

}
