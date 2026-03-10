package sk.tany.rest.api.domain.contentsnippet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sk.tany.rest.api.domain.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentSnippet extends BaseEntity {
    private String name;
    private String placeholder;
    private String content;
}
