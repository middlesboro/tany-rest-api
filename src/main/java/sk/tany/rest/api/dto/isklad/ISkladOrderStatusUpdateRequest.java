package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ISkladOrderStatusUpdateRequest {
    @JsonProperty("order_original_id")
    private String orderOriginalId;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("status_id")
    private Integer statusId;
    @JsonProperty("status_name")
    private String statusName;
    private List<ISkladPackage> packages;
}
