package sk.tany.rest.api.dto.client.homepage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomepageGridResponse {
    private List<HomepageGridDto> homepageGrids;
}
