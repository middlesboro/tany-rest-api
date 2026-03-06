package sk.tany.rest.api.dto.admin.customeremail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEmailDto {

    private String id;
    private String email;
    private Boolean subscribed;
    private LocalDate subscribedDate;
    private Integer sentMails;
    private List<String> tags;

}
