package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.admin.customer.create.CustomerAdminCreateRequest;
import sk.tany.rest.api.dto.admin.customer.create.CustomerAdminCreateResponse;
import sk.tany.rest.api.dto.admin.customer.get.CustomerAdminGetResponse;
import sk.tany.rest.api.dto.admin.customer.list.CustomerAdminListResponse;
import sk.tany.rest.api.dto.admin.customer.update.CustomerAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.customer.update.CustomerAdminUpdateResponse;

@Mapper(componentModel = "spring")
public interface CustomerAdminApiMapper {
    @Mapping(target = "id", ignore = true)
    CustomerDto toDto(CustomerAdminCreateRequest request);
    CustomerAdminCreateResponse toCreateResponse(CustomerDto dto);

    CustomerAdminGetResponse toGetResponse(CustomerDto dto);

    CustomerAdminListResponse toListResponse(CustomerDto dto);

    @Mapping(target = "id", ignore = true)
    CustomerDto toDto(CustomerAdminUpdateRequest request);
    CustomerAdminUpdateResponse toUpdateResponse(CustomerDto dto);
}
