package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.mapper.CarrierMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarrierClientServiceImpl implements CarrierClientService {

    private final CarrierRepository carrierRepository;
    private final CarrierMapper carrierMapper;

    @Override
    public Page<CarrierDto> findAll(Pageable pageable) {
        return carrierRepository.findAll(pageable).map(carrierMapper::toDto);
    }

    @Override
    public Optional<CarrierDto> findById(String id) {
        return carrierRepository.findById(id).map(carrierMapper::toDto);
    }
}
