package sk.tany.rest.api.domain.emailnotification;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class EmailNotification implements BaseEntity {

    @Id
    private String id;
    private String email;
    private String productId;
    private Instant createDate;

    @Override
    public void setCreatedDate(Instant date) {
        this.createDate = date;
    }

    @Override
    public Instant getCreatedDate() {
        return this.createDate;
    }

    @Override
    public void setLastModifiedDate(Instant date) { }
    @Override
    public Instant getLastModifiedDate() { return null; }

}
