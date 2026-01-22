package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.pagecontent.PageContent;
import sk.tany.rest.api.domain.pagecontent.PageContentRepository;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.mapper.PageContentMapper;
import sk.tany.rest.api.service.admin.PageContentAdminService;
import sk.tany.rest.api.exception.PageContentException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageContentAdminServiceImpl implements PageContentAdminService {

    private final PageContentRepository repository;
    private final PageContentMapper mapper;

    @Override
    public Page<PageContentDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<PageContentDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public PageContentDto save(PageContentDto pageContentDto) {
        PageContent entity = mapper.toEntity(pageContentDto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public PageContentDto update(String id, PageContentDto pageContentDto) {
        PageContent entity = repository.findById(id)
                .orElseThrow(() -> new PageContentException.NotFound("Page content not found"));
        mapper.updateEntity(pageContentDto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public void deleteById(String id) {
        if (!repository.existsById(id)) {
            throw new PageContentException.NotFound("Page content not found");
        }
        repository.deleteById(id);
    }
}
