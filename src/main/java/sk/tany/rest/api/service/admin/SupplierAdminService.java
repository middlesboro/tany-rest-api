package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.SupplierDto;

import java.util.List;
import java.util.Optional;

public interface SupplierAdminService {
    Page<SupplierDto> findAll(Pageable pageable);
    List<SupplierDto> searchByQuery(String query);
    Optional<SupplierDto> findById(String id);
    SupplierDto save(SupplierDto supplierDto);
    SupplierDto update(String id, SupplierDto supplierDto);
    SupplierDto patch(String id, sk.tany.rest.api.dto.admin.supplier.patch.SupplierPatchRequest patchDto);
    void deleteById(String id);
    Optional<SupplierDto> findByPrestashopId(Long supplierId);
}
