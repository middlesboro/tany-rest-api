package sk.tany.rest.api.dto.admin.customeremail.patch;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerEmailPatchRequest {

    private String email;
    private Boolean subscribed;
    private LocalDate subscribedDate;
    private Integer sentMails;
    private List<String> tags;

}
