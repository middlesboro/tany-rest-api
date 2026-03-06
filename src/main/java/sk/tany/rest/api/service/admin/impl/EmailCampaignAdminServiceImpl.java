package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.emailcampaign.EmailCampaign;
import sk.tany.rest.api.domain.emailcampaign.EmailCampaignRepository;
import sk.tany.rest.api.dto.admin.emailcampaign.EmailCampaignDto;
import sk.tany.rest.api.dto.admin.emailcampaign.patch.EmailCampaignPatchRequest;
import sk.tany.rest.api.mapper.EmailCampaignMapper;
import sk.tany.rest.api.service.admin.EmailCampaignAdminService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailCampaignAdminServiceImpl implements EmailCampaignAdminService {

    private final EmailCampaignRepository repository;
    private final EmailCampaignMapper mapper;

    @Override
    public EmailCampaignDto save(EmailCampaignDto dto) {
        EmailCampaign entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Page<EmailCampaignDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<EmailCampaignDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public EmailCampaignDto update(String id, EmailCampaignDto dto) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromDto(dto, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EmailCampaign not found with id: " + id));
    }

    @Override
    public EmailCampaignDto patch(String id, EmailCampaignPatchRequest patch) {
        return repository.findById(id).map(entity -> {
            mapper.updateEntityFromPatch(patch, entity);
            return mapper.toDto(repository.save(entity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EmailCampaign not found with id: " + id));
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

}
