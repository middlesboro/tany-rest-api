package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.emailcampaign.EmailCampaign;
import sk.tany.rest.api.dto.admin.emailcampaign.EmailCampaignDto;
import sk.tany.rest.api.dto.admin.emailcampaign.patch.EmailCampaignPatchRequest;

@Mapper(componentModel = "spring")
public interface EmailCampaignMapper {

    EmailCampaignDto toDto(EmailCampaign entity);
    EmailCampaign toEntity(EmailCampaignDto dto);
    void updateEntityFromDto(EmailCampaignDto dto, @MappingTarget EmailCampaign entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(EmailCampaignPatchRequest patch, @MappingTarget EmailCampaign entity);

}
