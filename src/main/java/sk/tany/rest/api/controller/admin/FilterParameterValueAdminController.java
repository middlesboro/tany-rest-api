package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.admin.filterparametervalue.patch.FilterParameterValuePatchRequest;
import sk.tany.rest.api.service.admin.FilterParameterValueAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/filter-parameter-values")
@RequiredArgsConstructor
public class FilterParameterValueAdminController {

    private final FilterParameterValueAdminService service;

    @PostMapping
    public ResponseEntity<FilterParameterValueDto> create(@RequestBody FilterParameterValueDto dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<FilterParameterValueDto> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilterParameterValueDto> findById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FilterParameterValueDto> update(@PathVariable String id, @RequestBody FilterParameterValueDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FilterParameterValueDto> patch(@PathVariable String id, @RequestBody FilterParameterValuePatchRequest patch) {
        return ResponseEntity.ok(service.patch(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
