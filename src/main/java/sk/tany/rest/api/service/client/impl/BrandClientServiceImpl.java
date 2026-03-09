package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.mapper.BrandMapper;
import sk.tany.rest.api.service.client.BrandClientService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandClientServiceImpl implements BrandClientService {

    private final BrandRepository brandRepository;
    private final ProductSearchEngine productSearchEngine;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandDto> findAll() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .filter(brand -> productSearchEngine.hasActiveProductWithBrand(brand.getId()))
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }
}
