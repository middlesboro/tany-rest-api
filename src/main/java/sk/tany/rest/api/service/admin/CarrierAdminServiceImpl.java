package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.mapper.CarrierMapper;
import sk.tany.rest.api.service.common.ImageService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarrierAdminServiceImpl implements CarrierAdminService {

    private final CarrierRepository carrierRepository;
    private final CarrierMapper carrierMapper;
    private final ImageService imageService;

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
        carrierDto.setId(id);
        var carrier = carrierMapper.toEntity(carrierDto);
        var savedCarrier = carrierRepository.save(carrier);
        return carrierMapper.toDto(savedCarrier);
    }

    @Override
    public CarrierDto patch(String id, sk.tany.rest.api.dto.admin.carrier.patch.CarrierPatchRequest patchDto) {
        var carrier = carrierRepository.findById(id).orElseThrow(() -> new RuntimeException("Carrier not found"));
        carrierMapper.updateEntityFromPatch(patchDto, carrier);
        var savedCarrier = carrierRepository.save(carrier);
        return carrierMapper.toDto(savedCarrier);
    }

    @Override
    public void deleteById(String id) {
        var carrier = carrierRepository.findById(id);
        if (carrier.isPresent()) {
            if (carrier.get().getImage() != null) {
                imageService.delete(carrier.get().getImage());
            }
            carrierRepository.deleteById(id);
        }
    }
}
