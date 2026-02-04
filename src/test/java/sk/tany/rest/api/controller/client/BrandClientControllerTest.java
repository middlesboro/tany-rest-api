package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.client.BrandClientService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandClientControllerTest {

    @Mock
    private BrandClientService brandService;

    @InjectMocks
    private BrandClientController brandClientController;

    @Test
    void getBrands() {
        Page<BrandDto> brands = new PageImpl<>(Collections.singletonList(new BrandDto()));
        when(brandService.findAll(any(Pageable.class))).thenReturn(brands);

        Page<BrandDto> response = brandClientController.getBrands(Pageable.unpaged());

        assertEquals(brands, response);
        verify(brandService).findAll(any(Pageable.class));
    }
}
