package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.client.pagecontent.get.PageContentClientGetResponse;
import sk.tany.rest.api.mapper.PageContentClientApiMapper;
import sk.tany.rest.api.service.client.PageContentClientService;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageContentClientController {

    private final PageContentClientService pageContentService;
    private final PageContentClientApiMapper apiMapper;

    @GetMapping("/{slug}")
    public ResponseEntity<PageContentClientGetResponse> getPageBySlug(@PathVariable String slug) {
        return pageContentService.findBySlug(slug)
                .map(apiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
