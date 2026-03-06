package sk.tany.rest.api.domain.customeremail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.LocalDate;
import java.util.List;

@Document
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEmail extends BaseEntity {

    private String email;
    private Boolean subscribed;
    private LocalDate subscribedDate;
    private Integer sentMails;
    private List<String> tags;

}
