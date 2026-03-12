package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.contentsnippet.ContentSnippet;
import sk.tany.rest.api.domain.contentsnippet.ContentSnippetRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.ContentSnippetDto;
import sk.tany.rest.api.exception.ContentSnippetException;
import sk.tany.rest.api.mapper.ContentSnippetMapper;
import sk.tany.rest.api.service.admin.ContentSnippetAdminService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentSnippetAdminServiceImpl implements ContentSnippetAdminService {

    private final ContentSnippetRepository repository;
    private final ContentSnippetMapper mapper;
    private final ProductSearchEngine productSearchEngine;
    private final ProductRepository productRepository;

    @Override
    public Page<ContentSnippetDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public Optional<ContentSnippetDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public ContentSnippetDto save(ContentSnippetDto dto) {
        ContentSnippet entity = mapper.toEntity(dto);
        ContentSnippet saved = repository.save(entity);
        productSearchEngine.addContentSnippet(saved);
        updateProductsContainingSnippet(dto.getPlaceholder());
        return mapper.toDto(saved);
    }

    @Override
    public ContentSnippetDto update(String id, ContentSnippetDto dto) {
        ContentSnippet entity = repository.findById(id)
                .orElseThrow(() -> new ContentSnippetException.NotFound("Content snippet not found"));
        mapper.updateEntity(dto, entity);
        ContentSnippet updated = repository.save(entity);
        productSearchEngine.updateContentSnippet(updated);
        updateProductsContainingSnippet(dto.getPlaceholder());
        return mapper.toDto(updated);
    }

    private void updateProductsContainingSnippet(String placeholder) {
        if (placeholder == null || placeholder.isEmpty()) {
            return;
        }
        try (java.util.stream.Stream<Product> products = productRepository.findByDescriptionContaining(placeholder)) {
            products.forEach(product -> {
                productRepository.save(product);
                productSearchEngine.updateProduct(product);
            });
        }
    }

    @Override
    public void deleteById(String id) {
        if (!repository.existsById(id)) {
            throw new ContentSnippetException.NotFound("Content snippet not found");
        }
        repository.deleteById(id);
        productSearchEngine.removeContentSnippet(id);
    }
}
