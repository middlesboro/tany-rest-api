package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.ContentSnippetDto;
import sk.tany.rest.api.dto.admin.contentsnippet.create.ContentSnippetAdminCreateRequest;
import sk.tany.rest.api.dto.admin.contentsnippet.create.ContentSnippetAdminCreateResponse;
import sk.tany.rest.api.dto.admin.contentsnippet.get.ContentSnippetAdminGetResponse;
import sk.tany.rest.api.dto.admin.contentsnippet.list.ContentSnippetAdminListResponse;
import sk.tany.rest.api.dto.admin.contentsnippet.update.ContentSnippetAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.contentsnippet.update.ContentSnippetAdminUpdateResponse;
import sk.tany.rest.api.mapper.ContentSnippetAdminApiMapper;
import sk.tany.rest.api.service.admin.ContentSnippetAdminService;

import jakarta.validation.Valid;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/content-snippets")
@RequiredArgsConstructor
public class ContentSnippetAdminController {

    private final ContentSnippetAdminService contentSnippetAdminService;
    private final ContentSnippetAdminApiMapper apiMapper;

    @PostMapping
    public ResponseEntity<ContentSnippetAdminCreateResponse> create(@RequestBody @Valid ContentSnippetAdminCreateRequest request) {
        ContentSnippetDto dto = apiMapper.toDto(request);
        ContentSnippetDto savedDto = contentSnippetAdminService.save(dto);
        return new ResponseEntity<>(apiMapper.toCreateResponse(savedDto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<ContentSnippetAdminListResponse> getAll(Pageable pageable) {
        return contentSnippetAdminService.findAll(pageable)
                .map(apiMapper::toListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentSnippetAdminGetResponse> getById(@PathVariable String id) {
        return contentSnippetAdminService.findById(id)
                .map(apiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentSnippetAdminUpdateResponse> update(@PathVariable String id, @RequestBody @Valid ContentSnippetAdminUpdateRequest request) {
        ContentSnippetDto dto = apiMapper.toDto(request);
        ContentSnippetDto updatedDto = contentSnippetAdminService.update(id, dto);
        return ResponseEntity.ok(apiMapper.toUpdateResponse(updatedDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        contentSnippetAdminService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
