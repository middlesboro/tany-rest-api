package sk.tany.rest.api.domain.emailcampaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import sk.tany.rest.api.domain.BaseEntity;

import java.util.List;

@Document
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCampaign extends BaseEntity {

    private String name;
    private String templateId;
    private Boolean active;
    private List<String> tags;
    private Integer batchSize;

}
