package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CarrierDto;

import java.util.Optional;

public interface CarrierClientService {
    Page<CarrierDto> findAll(Pageable pageable);
    Optional<CarrierDto> findById(String id);
}
