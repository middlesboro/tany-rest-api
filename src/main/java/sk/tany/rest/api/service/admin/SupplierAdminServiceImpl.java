package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.mapper.ISkladMapper;
import sk.tany.rest.api.mapper.SupplierMapper;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierAdminServiceImpl implements SupplierAdminService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ISkladService iskladService;
    private final ISkladProperties iskladProperties;
    private final ISkladMapper iskladMapper;

    @Override
    public Page<SupplierDto> findAll(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(supplierMapper::toDto);
    }

    @Override
    public Optional<SupplierDto> findById(String id) {
        return supplierRepository.findById(id).map(supplierMapper::toDto);
    }

    @Override
    public SupplierDto save(SupplierDto supplierDto) {
        var supplier = supplierMapper.toEntity(supplierDto);
        var savedSupplier = supplierRepository.save(supplier);
        SupplierDto savedDto = supplierMapper.toDto(savedSupplier);

        if (iskladProperties.isEnabled()) {
            try {
                iskladService.createSupplier(iskladMapper.toCreateSupplierRequest(savedDto));
            } catch (Exception e) {
                // Log error but don't rollback local save, or decide policy. Assuming soft failure.
                // In real world, maybe we want to propagate or queue.
                // For now, simple logging is typical for this stage.
                System.err.println("Failed to sync supplier to iSklad: " + e.getMessage());
            }
        }

        return savedDto;
    }

    @Override
    public SupplierDto update(String id, SupplierDto supplierDto) {
        var supplier = supplierRepository.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplierDto.setId(id);
        supplierMapper.updateEntityFromDto(supplierDto, supplier);
        var savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    public SupplierDto patch(String id, sk.tany.rest.api.dto.admin.supplier.patch.SupplierPatchRequest patchDto) {
        var supplier = supplierRepository.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplierMapper.updateEntityFromPatch(patchDto, supplier);
        var savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    public void deleteById(String id) {
        supplierRepository.deleteById(id);
    }

    @Override
    public Optional<SupplierDto> findByPrestashopId(Long prestashopId) {
        return supplierRepository.findByPrestashopId(prestashopId).map(supplierMapper::toDto);
    }
}
