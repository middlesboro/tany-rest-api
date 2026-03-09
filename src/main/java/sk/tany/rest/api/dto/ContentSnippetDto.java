package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class ContentSnippetDto {
    private String id;
    private String name;
    private String placeholder;
    private String content;
}
