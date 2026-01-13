package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.pagecontent.PageContentRepository;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.mapper.PageContentMapper;
import sk.tany.rest.api.service.client.PageContentClientService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageContentClientServiceImpl implements PageContentClientService {

    private final PageContentRepository repository;
    private final PageContentMapper mapper;

    @Override
    public Optional<PageContentDto> findBySlug(String slug) {
        return repository.findBySlug(slug).map(mapper::toDto);
    }
}
