package sk.tany.rest.api.domain.onedrive;

import lombok.Data;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class OneDriveToken implements BaseEntity {
    @Id
    private String id;
    private String refreshToken;
    private Instant updateDate;

    @Override
    public void setCreatedDate(Instant date) { }
    @Override
    public Instant getCreatedDate() { return null; }
    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
