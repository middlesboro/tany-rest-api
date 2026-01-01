package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CarrierPriceRangeDto;

@Mapper(componentModel = "spring")
public interface CarrierMapper {
    CarrierDto toDto(Carrier carrier);
    Carrier toEntity(CarrierDto carrierDto);
    CarrierPriceRangeDto toDto(CarrierPriceRange range);
    CarrierPriceRange toEntity(CarrierPriceRangeDto rangeDto);
    void updateEntityFromDto(CarrierDto carrierDto, @MappingTarget Carrier carrier);
}
