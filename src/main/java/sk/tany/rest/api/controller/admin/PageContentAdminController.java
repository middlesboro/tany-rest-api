package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.dto.admin.pagecontent.create.PageContentAdminCreateRequest;
import sk.tany.rest.api.dto.admin.pagecontent.create.PageContentAdminCreateResponse;
import sk.tany.rest.api.dto.admin.pagecontent.get.PageContentAdminGetResponse;
import sk.tany.rest.api.dto.admin.pagecontent.list.PageContentAdminListResponse;
import sk.tany.rest.api.dto.admin.pagecontent.update.PageContentAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.pagecontent.update.PageContentAdminUpdateResponse;
import sk.tany.rest.api.mapper.PageContentAdminApiMapper;
import sk.tany.rest.api.service.admin.PageContentAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/pages")
@RequiredArgsConstructor
public class PageContentAdminController {

    private final PageContentAdminService pageContentService;
    private final PageContentAdminApiMapper apiMapper;

    @PostMapping
    public ResponseEntity<PageContentAdminCreateResponse> createPage(@RequestBody PageContentAdminCreateRequest request) {
        PageContentDto dto = apiMapper.toDto(request);
        PageContentDto savedDto = pageContentService.save(dto);
        return new ResponseEntity<>(apiMapper.toCreateResponse(savedDto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<PageContentAdminListResponse> getPages(Pageable pageable) {
        return pageContentService.findAll(pageable)
                .map(apiMapper::toListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageContentAdminGetResponse> getPage(@PathVariable String id) {
        return pageContentService.findById(id)
                .map(apiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageContentAdminUpdateResponse> updatePage(@PathVariable String id, @RequestBody PageContentAdminUpdateRequest request) {
        PageContentDto dto = apiMapper.toDto(request);
        PageContentDto updatedDto = pageContentService.update(id, dto);
        return ResponseEntity.ok(apiMapper.toUpdateResponse(updatedDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable String id) {
        pageContentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
