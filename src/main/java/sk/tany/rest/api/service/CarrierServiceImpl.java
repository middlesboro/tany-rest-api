package sk.tany.rest.api.service;

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
public class CarrierServiceImpl implements CarrierService {

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

    @Override
    public CarrierDto save(CarrierDto carrierDto) {
        var carrier = carrierMapper.toEntity(carrierDto);
        var savedCarrier = carrierRepository.save(carrier);
        return carrierMapper.toDto(savedCarrier);
    }

    @Override
    public CarrierDto update(String id, CarrierDto carrierDto) {
        var carrier = carrierRepository.findById(id).orElseThrow(() -> new RuntimeException("Carrier not found"));
        carrierDto.setId(id);
        carrierMapper.updateEntityFromDto(carrierDto, carrier);
        var savedCarrier = carrierRepository.save(carrier);
        return carrierMapper.toDto(savedCarrier);
    }

    @Override
    public void deleteById(String id) {
        carrierRepository.deleteById(id);
    }
}
