package sk.tany.rest.api.dto.admin.customermessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMessageDto {

    private String id;
    private String message;
    private String email;
    private Instant createDate;

}
