package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.mapper.BrandMapper;
import sk.tany.rest.api.service.client.BrandClientService;

@Service
@RequiredArgsConstructor
public class BrandClientServiceImpl implements BrandClientService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public Page<BrandDto> findAll(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(brandMapper::toDto);
    }
}
