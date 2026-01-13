package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.service.client.BlogClientService;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogClientController {

    private final BlogClientService blogService;

    @GetMapping
    public List<BlogDto> getAll() {
        return blogService.getAll();
    }
}
