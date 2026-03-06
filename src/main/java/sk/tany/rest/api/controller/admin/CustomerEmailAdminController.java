package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.customeremail.CustomerEmailDto;
import sk.tany.rest.api.dto.admin.customeremail.patch.CustomerEmailPatchRequest;
import sk.tany.rest.api.service.admin.CustomerEmailAdminService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/customer-emails")
@RequiredArgsConstructor
public class CustomerEmailAdminController {

    private final CustomerEmailAdminService service;

    @PostMapping
    public ResponseEntity<CustomerEmailDto> create(@RequestBody CustomerEmailDto dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<CustomerEmailDto> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerEmailDto> findById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerEmailDto> update(@PathVariable String id, @RequestBody CustomerEmailDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerEmailDto> patch(@PathVariable String id, @RequestBody CustomerEmailPatchRequest patch) {
        return ResponseEntity.ok(service.patch(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
