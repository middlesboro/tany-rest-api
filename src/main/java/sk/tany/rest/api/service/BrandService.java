package sk.tany.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.BrandDto;

import java.util.Optional;

public interface BrandService {
    Page<BrandDto> findAll(Pageable pageable);
    Optional<BrandDto> findById(String id);
    BrandDto save(BrandDto brandDto);
    BrandDto update(String id, BrandDto brandDto);
    void deleteById(String id);
}
