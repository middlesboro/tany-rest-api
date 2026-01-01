package sk.tany.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CarrierDto;

import java.util.Optional;

public interface CarrierService {
    Page<CarrierDto> findAll(Pageable pageable);
    Optional<CarrierDto> findById(String id);
    CarrierDto save(CarrierDto carrierDto);
    CarrierDto update(String id, CarrierDto carrierDto);
    void deleteById(String id);
}
