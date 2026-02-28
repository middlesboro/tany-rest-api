package sk.tany.rest.api.dto.admin.customermessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.domain.customermessage.MessageType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMessageDto {

    private String id;
    private String message;
    private MessageType type;
    private String email;
    private Instant createDate;

}
