package sk.tany.rest.api.dto.admin.pagecontent.create;

import lombok.Data;

@Data
public class PageContentAdminCreateRequest {
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private boolean visible;
}
