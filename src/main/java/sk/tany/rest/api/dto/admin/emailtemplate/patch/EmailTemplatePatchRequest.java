package sk.tany.rest.api.dto.admin.emailtemplate.patch;

import lombok.Data;

@Data
public class EmailTemplatePatchRequest {

    private String name;
    private String content;
    private Boolean active;

}
