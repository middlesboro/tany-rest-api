package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.BlogDto;
import java.util.List;

public interface BlogClientService {
    List<BlogDto> getAll();
}
