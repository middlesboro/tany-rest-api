package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.emailtemplate.EmailTemplate;
import sk.tany.rest.api.dto.admin.emailtemplate.EmailTemplateDto;
import sk.tany.rest.api.dto.admin.emailtemplate.patch.EmailTemplatePatchRequest;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {

    EmailTemplateDto toDto(EmailTemplate entity);
    EmailTemplate toEntity(EmailTemplateDto dto);
    void updateEntityFromDto(EmailTemplateDto dto, @MappingTarget EmailTemplate entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(EmailTemplatePatchRequest patch, @MappingTarget EmailTemplate entity);

}
