package sk.tany.rest.api.dto.admin.contentsnippet.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContentSnippetAdminUpdateRequest {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String placeholder;
    @NotBlank
    private String content;
}
