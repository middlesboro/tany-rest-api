package sk.tany.rest.api.domain.mailplatform;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Document(collection = "mail_platforms")
public class MailPlatform extends BaseEntity {

    private String name;
    private MailPlatformType platformType;
    private List<ListType> listTypes;

    private int limitPerDay;
    private int limitPerMonth;

    private boolean active;

    private int sentPerDay;
    private int sentPerMonth;

    private LocalDate lastSentDate;

}
