package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.dto.admin.supplier.patch.SupplierPatchRequest;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierDto toDto(Supplier supplier);
    Supplier toEntity(SupplierDto supplierDto);
    void updateEntityFromDto(SupplierDto supplierDto, @MappingTarget Supplier supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(SupplierPatchRequest patch, @MappingTarget Supplier supplier);
}
