package sk.tany.rest.api.dto.admin.emailtemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {

    private String id;
    private String name;
    private String content;
    private Boolean active;

}
