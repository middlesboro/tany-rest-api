package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.ContentSnippetDto;

import java.util.Optional;

public interface ContentSnippetAdminService {
    Page<ContentSnippetDto> findAll(Pageable pageable);
    Optional<ContentSnippetDto> findById(String id);
    ContentSnippetDto save(ContentSnippetDto dto);
    ContentSnippetDto update(String id, ContentSnippetDto dto);
    void deleteById(String id);
}
