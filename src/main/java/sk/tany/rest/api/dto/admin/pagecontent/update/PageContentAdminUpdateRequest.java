package sk.tany.rest.api.dto.admin.pagecontent.update;

import lombok.Data;

@Data
public class PageContentAdminUpdateRequest {
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private boolean visible;
}
