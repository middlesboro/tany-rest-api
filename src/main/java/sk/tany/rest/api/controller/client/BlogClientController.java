package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.dto.client.blog.get.BlogClientGetResponse;
import sk.tany.rest.api.mapper.BlogClientApiMapper;
import sk.tany.rest.api.service.client.BlogClientService;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogClientController {

    private final BlogClientService blogService;
    private final BlogClientApiMapper blogClientApiMapper;

    @GetMapping
    public List<BlogDto> getAll() {
        return blogService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogClientGetResponse> getBlog(@PathVariable String id) {
        return blogService.getBlog(id)
                .map(blogClientApiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
