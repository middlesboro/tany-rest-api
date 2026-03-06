package sk.tany.rest.api.dto.admin.emailcampaign.patch;

import lombok.Data;

import java.util.List;

@Data
public class EmailCampaignPatchRequest {

    private String name;
    private String templateId;
    private Boolean active;
    private List<String> tags;
    private Integer batchSize;

}
