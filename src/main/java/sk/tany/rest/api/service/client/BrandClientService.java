package sk.tany.rest.api.service.client;

import java.util.List;
import sk.tany.rest.api.dto.BrandDto;

public interface BrandClientService {
    List<BrandDto> findAll();
}
