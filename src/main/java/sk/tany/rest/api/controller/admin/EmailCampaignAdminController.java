package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.emailcampaign.EmailCampaignDto;
import sk.tany.rest.api.dto.admin.emailcampaign.patch.EmailCampaignPatchRequest;
import sk.tany.rest.api.service.admin.EmailCampaignAdminService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/email-campaigns")
@RequiredArgsConstructor
public class EmailCampaignAdminController {

    private final EmailCampaignAdminService service;

    @PostMapping
    public ResponseEntity<EmailCampaignDto> create(@RequestBody EmailCampaignDto dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<EmailCampaignDto> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignDto> findById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailCampaignDto> update(@PathVariable String id, @RequestBody EmailCampaignDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmailCampaignDto> patch(@PathVariable String id, @RequestBody EmailCampaignPatchRequest patch) {
        return ResponseEntity.ok(service.patch(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
