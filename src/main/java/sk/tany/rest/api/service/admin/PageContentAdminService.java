package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.PageContentDto;
import java.util.Optional;

public interface PageContentAdminService {
    Page<PageContentDto> findAll(Pageable pageable);
    Optional<PageContentDto> findById(String id);
    PageContentDto save(PageContentDto pageContentDto);
    PageContentDto update(String id, PageContentDto pageContentDto);
    void deleteById(String id);
}
