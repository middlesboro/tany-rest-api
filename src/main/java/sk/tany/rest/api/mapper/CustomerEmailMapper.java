package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.customeremail.CustomerEmail;
import sk.tany.rest.api.dto.admin.customeremail.CustomerEmailDto;
import sk.tany.rest.api.dto.admin.customeremail.patch.CustomerEmailPatchRequest;

@Mapper(componentModel = "spring")
public interface CustomerEmailMapper {

    CustomerEmailDto toDto(CustomerEmail entity);
    CustomerEmail toEntity(CustomerEmailDto dto);
    void updateEntityFromDto(CustomerEmailDto dto, @MappingTarget CustomerEmail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(CustomerEmailPatchRequest patch, @MappingTarget CustomerEmail entity);

}
