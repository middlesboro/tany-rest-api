package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.dto.isklad.CreateBrandRequest;
import sk.tany.rest.api.exception.BrandException;
import sk.tany.rest.api.dto.admin.brand.BrandAdminGetResponse;
import sk.tany.rest.api.mapper.BrandMapper;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandAdminServiceImpl implements BrandAdminService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final BrandMapper brandMapper;
    private final ImageService imageService;
    private final ISkladService iSkladService;

    @Override
    public Page<BrandDto> findAll(Pageable pageable) {
        return brandRepository.findAll(pageable).map(brandMapper::toDto);
    }

    @Override
    public Optional<BrandDto> findById(String id) {
        return brandRepository.findById(id).map(brandMapper::toDto);
    }

    @Override
    public Optional<BrandAdminGetResponse> findDetailById(String id) {
        return brandRepository.findById(id).map(brand -> {
            var productIds = productRepository.findAllByBrandId(id).stream()
                    .map(Product::getId)
                    .toList();
            return brandMapper.toAdminDetailDto(brand, productIds);
        });
    }

    @Override
    public BrandDto save(BrandDto brandDto) {
        brandDto.setName(StringUtils.trim(brandDto.getName()));
        var brand = brandMapper.toEntity(brandDto);
        var savedBrand = brandRepository.save(brand);

        CreateBrandRequest createBrandRequest = new CreateBrandRequest();
        createBrandRequest.setName(brandDto.getName());
        createBrandRequest.setCountryCode("SK");
        iSkladService.createBrand(createBrandRequest);

        return brandMapper.toDto(savedBrand);
    }

    @Override
    public BrandDto update(String id, BrandDto brandDto) {
        var brand = brandRepository.findById(id).orElseThrow(() -> new BrandException.NotFound("Brand not found"));
        brandDto.setId(id);
        brandDto.setName(StringUtils.trim(brandDto.getName()));
        brandMapper.updateEntityFromDto(brandDto, brand);
        var savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    @Override
    public BrandDto patch(String id, sk.tany.rest.api.dto.admin.brand.patch.BrandPatchRequest patchDto) {
        var brand = brandRepository.findById(id).orElseThrow(() -> new BrandException.NotFound("Brand not found"));
        brandMapper.updateEntityFromPatch(patchDto, brand);
        var savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    @Override
    public void deleteById(String id) {
        var brand = brandRepository.findById(id);
        if (brand.isPresent()) {
            if (brand.get().getImage() != null) {
                imageService.delete(brand.get().getImage());
            }
            brandRepository.deleteById(id);
        }
    }

    @Override
    public Optional<BrandDto> findByPrestashopId(Long prestashopId) {
        return brandRepository.findByPrestashopId(prestashopId).map(brandMapper::toDto);
    }
}
