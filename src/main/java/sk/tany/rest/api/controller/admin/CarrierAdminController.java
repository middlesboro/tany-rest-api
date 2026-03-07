package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.admin.carrier.patch.CarrierPatchRequest;
import sk.tany.rest.api.service.admin.CarrierAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/carriers")
@RequiredArgsConstructor
public class CarrierAdminController {

    private final CarrierAdminService carrierService;
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<CarrierDto> createCarrier(@RequestBody CarrierDto carrier) {
        CarrierDto savedCarrier = carrierService.save(carrier);
        return new ResponseEntity<>(savedCarrier, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<CarrierDto> getCarriers(Pageable pageable) {
        return carrierService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierDto> getCarrier(@PathVariable String id) {
        return carrierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarrierDto> updateCarrier(@PathVariable String id, @RequestBody CarrierDto carrier) {
        CarrierDto updatedCarrier = carrierService.update(id, carrier);
        return ResponseEntity.ok(updatedCarrier);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CarrierDto> patchCarrier(@PathVariable String id, @RequestBody CarrierPatchRequest patchDto) {
        CarrierDto updatedCarrier = carrierService.patch(id, patchDto);
        return ResponseEntity.ok(updatedCarrier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable String id) {
        carrierService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<CarrierDto> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return carrierService.findById(id)
                .map(carrier -> {
                    try {
                        String extension = org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename());
                        if (org.apache.commons.lang3.StringUtils.isBlank(extension)) {
                            extension = "jpg";
                        }

                        String baseName = carrier.getName() != null ? carrier.getName().toLowerCase().replaceAll("[^a-z0-9-]", "-") : id;
                        String filename = baseName + "." + extension;

                        String imageUrl = imageService.upload(file.getBytes(), filename, ImageKitType.CARRIER);
                        carrier.setImage(imageUrl);
                        CarrierDto updatedCarrier = carrierService.update(id, carrier);
                        return ResponseEntity.ok(updatedCarrier);
                    } catch (java.io.IOException e) {
                        throw new RuntimeException("Failed to read image file", e);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        return carrierService.findById(id)
                .map(carrier -> {
                    if (carrier.getImage() != null) {
                        imageService.delete(carrier.getImage());
                        carrier.setImage(null);
                        carrierService.update(id, carrier);
                    }
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
