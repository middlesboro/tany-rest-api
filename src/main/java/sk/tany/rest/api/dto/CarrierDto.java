package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.domain.carrier.CarrierType;

import java.time.Instant;
import java.util.List;

@Data
public class CarrierDto {
    private String id;
    private String name;
    private String description;
    private String image;
    private Integer order;
    private CarrierType type;
    private List<CarrierPriceRangeDto> ranges;
    private Instant createdDate;
    private Instant updateDate;
    private boolean selected;
}
