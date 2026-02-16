package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ISkladPackage {
    @JsonProperty("package_nr")
    private String packageNr;
    @JsonProperty("tracking_url")
    private String trackingUrl;
    // other fields can be ignored for now
}
