package sk.tany.rest.api.dto.admin.contentsnippet.update;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ContentSnippetAdminUpdateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String placeholder;
    @NotBlank
    private String content;
}
