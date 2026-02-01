package sk.tany.rest.api.domain;

import java.time.Instant;

public interface BaseEntity {
    String getId();
    void setId(String id);

    void setCreatedDate(Instant date);
    Instant getCreatedDate();

    void setLastModifiedDate(Instant date);
    Instant getLastModifiedDate();

    default Object getSortValue(String field) {
        if ("id".equals(field)) {
            return getId();
        }
        if ("createDate".equals(field) || "createdDate".equals(field)) {
            return getCreatedDate();
        }
        if ("updateDate".equals(field) || "lastModifiedDate".equals(field)) {
            return getLastModifiedDate();
        }
        return null;
    }
}
