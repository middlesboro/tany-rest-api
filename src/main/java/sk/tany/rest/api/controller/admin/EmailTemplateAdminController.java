package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.emailtemplate.EmailTemplateDto;
import sk.tany.rest.api.dto.admin.emailtemplate.patch.EmailTemplatePatchRequest;
import sk.tany.rest.api.service.admin.EmailTemplateAdminService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/email-templates")
@RequiredArgsConstructor
public class EmailTemplateAdminController {

    private final EmailTemplateAdminService service;

    @PostMapping
    public ResponseEntity<EmailTemplateDto> create(@RequestBody EmailTemplateDto dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<EmailTemplateDto> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateDto> findById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplateDto> update(@PathVariable String id, @RequestBody EmailTemplateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmailTemplateDto> patch(@PathVariable String id, @RequestBody EmailTemplatePatchRequest patch) {
        return ResponseEntity.ok(service.patch(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
