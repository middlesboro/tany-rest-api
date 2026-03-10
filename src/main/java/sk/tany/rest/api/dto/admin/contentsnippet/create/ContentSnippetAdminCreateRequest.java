package sk.tany.rest.api.dto.admin.contentsnippet.create;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ContentSnippetAdminCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String placeholder;
    @NotBlank
    private String content;
}
