package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.admin.BrandAdminService;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
public class BrandAdminController {

    private final BrandAdminService brandService;
    private final ImageService imageService;
    private final PrestaShopImportService prestaShopImportService;

    @PostMapping("/import/prestashop")
    public void importFromPrestaShop() {
        prestaShopImportService.importAllManufacturers();
    }

    @PostMapping
    public ResponseEntity<BrandDto> createBrand(@RequestBody BrandDto brand) {
        BrandDto savedBrand = brandService.save(brand);
        return new ResponseEntity<>(savedBrand, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<BrandDto> getBrands(Pageable pageable) {
        return brandService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDto> getBrand(@PathVariable String id) {
        return brandService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDto> updateBrand(@PathVariable String id, @RequestBody BrandDto brand) {
        BrandDto updatedBrand = brandService.update(id, brand);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable String id) {
        brandService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<BrandDto> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return brandService.findById(id)
                .map(brand -> {
                    String imageUrl = imageService.upload(file, ImageKitType.BRAND);
                    brand.setImage(imageUrl);
                    BrandDto updatedBrand = brandService.update(id, brand);
                    return ResponseEntity.ok(updatedBrand);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        return brandService.findById(id)
                .map(brand -> {
                    if (brand.getImage() != null) {
                        imageService.delete(brand.getImage());
                        brand.setImage(null);
                        brandService.update(id, brand);
                    }
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
