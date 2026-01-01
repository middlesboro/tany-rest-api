package sk.tany.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.SupplierDto;

import java.util.Optional;

public interface SupplierService {
    Page<SupplierDto> findAll(Pageable pageable);
    Optional<SupplierDto> findById(String id);
    SupplierDto save(SupplierDto supplierDto);
    SupplierDto update(String id, SupplierDto supplierDto);
    void deleteById(String id);
}
