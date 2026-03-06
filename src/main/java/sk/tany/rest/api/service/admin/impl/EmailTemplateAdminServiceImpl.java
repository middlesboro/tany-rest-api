package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.emailtemplate.EmailTemplate;
import sk.tany.rest.api.domain.emailtemplate.EmailTemplateRepository;
import sk.tany.rest.api.dto.admin.emailtemplate.EmailTemplateDto;
import sk.tany.rest.api.dto.admin.emailtemplate.patch.EmailTemplatePatchRequest;
import sk.tany.rest.api.mapper.EmailTemplateMapper;
import sk.tany.rest.api.service.admin.EmailTemplateAdminService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailTemplateAdminServiceImpl implements EmailTemplateAdminService {

    private final EmailTemplateRepository repository;
    private final EmailTemplateMapper mapper;

    @Override
    public EmailTemplateDto save(EmailTemplateDto dto) {
        EmailTemplate entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Page<EmailTemplateDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<EmailTemplateDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public EmailTemplateDto update(String id, EmailTemplateDto dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDto(dto, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EmailTemplate not found with id: " + id));
    }

    @Override
    public EmailTemplateDto patch(String id, EmailTemplatePatchRequest patch) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromPatch(patch, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EmailTemplate not found with id: " + id));
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

}
