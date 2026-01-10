package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.BrandDto;

import java.util.Optional;

public interface BrandAdminService {
    Page<BrandDto> findAll(Pageable pageable);
    Optional<BrandDto> findById(String id);
    BrandDto save(BrandDto brandDto);
    BrandDto update(String id, BrandDto brandDto);
    BrandDto patch(String id, sk.tany.rest.api.dto.admin.brand.patch.BrandPatchRequest patchDto);
    void deleteById(String id);
    Optional<BrandDto> findByPrestashopId(Long brandPrestashopId);
}
