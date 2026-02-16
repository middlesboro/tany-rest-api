package sk.tany.rest.api.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseEntity {

    @Id
    private String id;

    @CreatedDate
    private Instant createDate;

    @LastModifiedDate
    private Instant updateDate;

    // Backward compatibility methods for getSortValue if strictly needed by ProductSearchEngine,
    // but typically we can access fields directly if getters are present.
    // However, the interface had getSortValue default method. I'll preserve it or adapted logic.

    public Object getSortValue(String field) {
        if ("id".equals(field)) {
            return getId();
        }
        if ("createDate".equals(field) || "createdDate".equals(field)) {
            return getCreateDate();
        }
        if ("updateDate".equals(field) || "lastModifiedDate".equals(field)) {
            return getUpdateDate();
        }
        return null;
    }

}
