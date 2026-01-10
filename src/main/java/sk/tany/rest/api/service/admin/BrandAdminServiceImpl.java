package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.mapper.BrandMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandAdminServiceImpl implements BrandAdminService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public Page<BrandDto> findAll(Pageable pageable) {
        return brandRepository.findAll(pageable).map(brandMapper::toDto);
    }

    @Override
    public Optional<BrandDto> findById(String id) {
        return brandRepository.findById(id).map(brandMapper::toDto);
    }

    @Override
    public BrandDto save(BrandDto brandDto) {
        var brand = brandMapper.toEntity(brandDto);
        var savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    @Override
    public BrandDto update(String id, BrandDto brandDto) {
        var brand = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Brand not found"));
        brandDto.setId(id);
        brandMapper.updateEntityFromDto(brandDto, brand);
        var savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    @Override
    public BrandDto patch(String id, sk.tany.rest.api.dto.admin.brand.patch.BrandPatchRequest patchDto) {
        var brand = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Brand not found"));
        brandMapper.updateEntityFromPatch(patchDto, brand);
        var savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    @Override
    public void deleteById(String id) {
        brandRepository.deleteById(id);
    }

    @Override
    public Optional<BrandDto> findByPrestashopId(Long prestashopId) {
        return brandRepository.findByPrestashopId(prestashopId).map(brandMapper::toDto);
    }
}
