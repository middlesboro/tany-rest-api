package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.homepage.HomepageGridAdminDto;
import sk.tany.rest.api.dto.admin.homepage.patch.HomepageGridPatchRequest;
import sk.tany.rest.api.service.admin.HomepageGridAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/homepage-grids")
@RequiredArgsConstructor
public class HomepageGridAdminController {

    private final HomepageGridAdminService homepageGridAdminService;

    @GetMapping
    public Page<HomepageGridAdminDto> getHomepageGrids(Pageable pageable) {
        return homepageGridAdminService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomepageGridAdminDto> getHomepageGrid(@PathVariable String id) {
        return homepageGridAdminService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HomepageGridAdminDto> createHomepageGrid(@RequestBody HomepageGridAdminDto dto) {
        HomepageGridAdminDto created = homepageGridAdminService.save(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HomepageGridAdminDto> updateHomepageGrid(@PathVariable String id, @RequestBody HomepageGridAdminDto dto) {
        HomepageGridAdminDto updated = homepageGridAdminService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HomepageGridAdminDto> patchHomepageGrid(@PathVariable String id, @RequestBody HomepageGridPatchRequest dto) {
        HomepageGridAdminDto updated = homepageGridAdminService.patch(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomepageGrid(@PathVariable String id) {
        homepageGridAdminService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
