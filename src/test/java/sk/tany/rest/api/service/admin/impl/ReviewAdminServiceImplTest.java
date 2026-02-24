package sk.tany.rest.api.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.admin.review.ReviewAdminCreateRequest;
import sk.tany.rest.api.dto.admin.review.ReviewAdminDetailResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminListResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminUpdateRequest;
import sk.tany.rest.api.service.mapper.ReviewMapper;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewAdminServiceImplTest {

    @Mock
    private ReviewRepository repository;

    @Mock
    private ReviewMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReviewAdminServiceImpl service;

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Review review = new Review();
        ReviewAdminListResponse response = new ReviewAdminListResponse();

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.singletonList(review)));
        when(mapper.toAdminListResponse(review)).thenReturn(response);

        Page<ReviewAdminListResponse> result = service.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().getFirst());
    }

    @Test
    void findById() {
        String id = "1";
        Review review = new Review();
        ReviewAdminDetailResponse response = new ReviewAdminDetailResponse();

        when(repository.findById(id)).thenReturn(Optional.of(review));
        when(mapper.toAdminDetailResponse(review)).thenReturn(response);

        ReviewAdminDetailResponse result = service.findById(id);

        assertEquals(response, result);
    }

    @Test
    void create() {
        ReviewAdminCreateRequest request = new ReviewAdminCreateRequest();
        Review review = new Review();
        Review savedReview = new Review();
        ReviewAdminDetailResponse response = new ReviewAdminDetailResponse();

        when(mapper.toEntity(request)).thenReturn(review);
        when(repository.save(review)).thenReturn(savedReview);
        when(mapper.toAdminDetailResponse(savedReview)).thenReturn(response);

        ReviewAdminDetailResponse result = service.create(request);

        assertEquals(response, result);
    }

    @Test
    void update() {
        String id = "1";
        ReviewAdminUpdateRequest request = new ReviewAdminUpdateRequest();
        Review review = new Review();
        Review savedReview = new Review();
        ReviewAdminDetailResponse response = new ReviewAdminDetailResponse();

        when(repository.findById(id)).thenReturn(Optional.of(review));
        when(repository.save(review)).thenReturn(savedReview);
        when(mapper.toAdminDetailResponse(savedReview)).thenReturn(response);

        ReviewAdminDetailResponse result = service.update(id, request);

        verify(mapper).updateEntityFromRequest(request, review);
        assertEquals(response, result);
    }

    @Test
    void delete() {
        String id = "1";
        when(repository.findById(id)).thenReturn(Optional.of(new Review()));

        service.delete(id);

        verify(repository).deleteById(id);
    }
}
