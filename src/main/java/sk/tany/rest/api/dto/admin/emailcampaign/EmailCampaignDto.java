package sk.tany.rest.api.dto.admin.emailcampaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCampaignDto {

    private String id;
    private String name;
    private String templateId;
    private Boolean active;
    private List<String> tags;
    private Integer batchSize;

}
