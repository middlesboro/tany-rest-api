package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;
import sk.tany.rest.api.service.client.ReviewClientService;
import sk.tany.rest.api.service.mapper.ReviewMapper;

@Service
@RequiredArgsConstructor
public class ReviewClientServiceImpl implements ReviewClientService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Override
    public Page<ReviewClientListResponse> findAllByProductId(String productId, Pageable pageable) {
        return repository.findAllByProductId(productId, pageable)
                .map(mapper::toClientListResponse);
    }
}
