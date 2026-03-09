package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import sk.tany.rest.api.domain.supplier.SupplierInvoice;
import sk.tany.rest.api.dto.SupplierInvoiceAdminDto;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierInvoiceAdminMapper {

    SupplierInvoiceAdminDto toDto(SupplierInvoice entity);

    List<SupplierInvoiceAdminDto> toDtoList(List<SupplierInvoice> entities);

    SupplierInvoice toEntity(SupplierInvoiceAdminDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SupplierInvoiceAdminDto dto, @MappingTarget SupplierInvoice entity);
}
