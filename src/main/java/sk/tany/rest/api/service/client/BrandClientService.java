package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.BrandDto;

public interface BrandClientService {
    Page<BrandDto> findAll(Pageable pageable);
}
