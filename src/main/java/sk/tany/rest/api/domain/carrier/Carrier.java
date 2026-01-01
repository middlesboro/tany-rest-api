package sk.tany.rest.api.domain.carrier;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "carriers")
public class Carrier {
    @Id
    private String id;
    private String name;
    private String description;
    private String image;
    private Integer order;
    private CarrierType type;
    private List<CarrierPriceRange> ranges;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant updateDate;
}
