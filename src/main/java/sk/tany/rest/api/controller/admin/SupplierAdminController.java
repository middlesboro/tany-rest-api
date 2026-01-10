package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.admin.SupplierAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class SupplierAdminController {

    private final SupplierAdminService supplierService;
    private final PrestaShopImportService prestaShopImportService;

    @PostMapping("/import/prestashop")
    public void importFromPrestaShop() {
        prestaShopImportService.importAllSuppliers();
    }

    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(@RequestBody SupplierDto supplier) {
        SupplierDto savedSupplier = supplierService.save(supplier);
        return new ResponseEntity<>(savedSupplier, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<SupplierDto> getSuppliers(Pageable pageable) {
        return supplierService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplier(@PathVariable String id) {
        return supplierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> updateSupplier(@PathVariable String id, @RequestBody SupplierDto supplier) {
        SupplierDto updatedSupplier = supplierService.update(id, supplier);
        return ResponseEntity.ok(updatedSupplier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable String id) {
        supplierService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
