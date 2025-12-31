package sk.tany.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerDto {

    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String cartId;
}
