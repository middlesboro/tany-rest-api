package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.productlabel.ProductLabel;
import sk.tany.rest.api.domain.productlabel.ProductLabelRepository;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.mapper.ProductLabelMapper;
import sk.tany.rest.api.service.admin.ProductLabelAdminService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductLabelAdminServiceImpl implements ProductLabelAdminService {

    private final ProductLabelRepository productLabelRepository;
    private final ProductLabelMapper productLabelMapper;

    @Override
    public ProductLabelDto save(ProductLabelDto productLabelDto) {
        ProductLabel productLabel = productLabelMapper.toEntity(productLabelDto);
        productLabel = productLabelRepository.save(productLabel);
        return productLabelMapper.toDto(productLabel);
    }

    @Override
    public ProductLabelDto update(String id, ProductLabelDto productLabelDto) {
        ProductLabel existingProductLabel = productLabelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product label not found"));

        ProductLabel productLabel = productLabelMapper.toEntity(productLabelDto);
        productLabel.setId(existingProductLabel.getId()); // Ensure ID is preserved
        productLabel.setCreateDate(existingProductLabel.getCreateDate()); // Ensure createDate is preserved

        productLabel = productLabelRepository.save(productLabel);
        return productLabelMapper.toDto(productLabel);
    }

    @Override
    public void deleteById(String id) {
        productLabelRepository.deleteById(id);
    }

    @Override
    public Optional<ProductLabelDto> findById(String id) {
        return productLabelRepository.findById(id)
                .map(productLabelMapper::toDto);
    }

    @Override
    public Page<ProductLabelDto> findAll(Pageable pageable) {
        return productLabelRepository.findAll(pageable)
                .map(productLabelMapper::toDto);
    }

    @Override
    public List<ProductLabelDto> findAllByProductId(String productId) {
        return productLabelRepository.findAllByProductId(productId).stream()
                .map(productLabelMapper::toDto)
                .collect(Collectors.toList());
    }
}
