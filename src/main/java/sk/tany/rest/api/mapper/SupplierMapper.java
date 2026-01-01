package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.dto.SupplierDto;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDto toDto(Supplier supplier);
    Supplier toEntity(SupplierDto supplierDto);
    void updateEntityFromDto(SupplierDto supplierDto, @MappingTarget Supplier supplier);
}
