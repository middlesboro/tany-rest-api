package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.admin.productlabel.create.ProductLabelCreateRequest;
import sk.tany.rest.api.dto.admin.productlabel.create.ProductLabelCreateResponse;
import sk.tany.rest.api.dto.admin.productlabel.get.ProductLabelGetResponse;
import sk.tany.rest.api.dto.admin.productlabel.list.ProductLabelListResponse;
import sk.tany.rest.api.dto.admin.productlabel.update.ProductLabelUpdateRequest;
import sk.tany.rest.api.dto.admin.productlabel.update.ProductLabelUpdateResponse;
import sk.tany.rest.api.mapper.ProductLabelAdminApiMapper;
import sk.tany.rest.api.service.admin.ProductLabelAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/product-labels")
@RequiredArgsConstructor
public class ProductLabelAdminController {

    private final ProductLabelAdminService productLabelAdminService;
    private final ProductLabelAdminApiMapper productLabelAdminApiMapper;

    @PostMapping
    public ResponseEntity<ProductLabelCreateResponse> createProductLabel(@RequestBody ProductLabelCreateRequest request) {
        ProductLabelDto dto = productLabelAdminApiMapper.toDto(request);
        ProductLabelDto savedDto = productLabelAdminService.save(dto);
        return new ResponseEntity<>(productLabelAdminApiMapper.toCreateResponse(savedDto), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<ProductLabelListResponse> getAllProductLabels(Pageable pageable) {
        return productLabelAdminService.findAll(pageable)
                .map(productLabelAdminApiMapper::toListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductLabelGetResponse> getProductLabel(@PathVariable String id) {
        return productLabelAdminService.findById(id)
                .map(productLabelAdminApiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductLabelUpdateResponse> updateProductLabel(@PathVariable String id, @RequestBody ProductLabelUpdateRequest request) {
        ProductLabelDto dto = productLabelAdminApiMapper.toDto(request);
        ProductLabelDto updatedDto = productLabelAdminService.update(id, dto);
        return ResponseEntity.ok(productLabelAdminApiMapper.toUpdateResponse(updatedDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductLabel(@PathVariable String id) {
        productLabelAdminService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
