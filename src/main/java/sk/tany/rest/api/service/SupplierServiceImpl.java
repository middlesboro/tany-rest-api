package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.mapper.SupplierMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

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
        return supplierMapper.toDto(savedSupplier);
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
    public void deleteById(String id) {
        supplierRepository.deleteById(id);
    }
}
