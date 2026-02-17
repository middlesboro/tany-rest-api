package sk.tany.rest.api.domain.common;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Sequence extends BaseEntity {
    private Long seq;
}
