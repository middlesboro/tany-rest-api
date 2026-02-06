package sk.tany.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OneDriveFileDto {
    private String id;
    private String name;
    private OffsetDateTime createdDateTime;
}
