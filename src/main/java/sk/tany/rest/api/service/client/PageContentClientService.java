package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.PageContentDto;
import java.util.Optional;

public interface PageContentClientService {
    Optional<PageContentDto> findBySlug(String slug);
}
