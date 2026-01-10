package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CarrierPriceRangeDto;
import sk.tany.rest.api.dto.admin.carrier.patch.CarrierPatchRequest;

@Mapper(componentModel = "spring")
public interface CarrierMapper {
    CarrierDto toDto(Carrier carrier);
    Carrier toEntity(CarrierDto carrierDto);
    CarrierPriceRangeDto toDto(CarrierPriceRange range);
    CarrierPriceRange toEntity(CarrierPriceRangeDto rangeDto);
    void updateEntityFromDto(CarrierDto carrierDto, @MappingTarget Carrier carrier);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(CarrierPatchRequest patch, @MappingTarget Carrier carrier);
}
