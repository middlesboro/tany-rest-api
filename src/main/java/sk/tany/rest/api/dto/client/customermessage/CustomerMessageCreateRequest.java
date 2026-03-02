package sk.tany.rest.api.dto.client.customermessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.validation.CustomerMessageCreateConstraint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CustomerMessageCreateConstraint
public class CustomerMessageCreateRequest {

    private String message;
    private String email;

}
