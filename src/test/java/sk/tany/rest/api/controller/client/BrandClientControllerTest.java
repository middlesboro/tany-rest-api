package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.client.BrandClientService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        List<BrandDto> brands = Collections.singletonList(new BrandDto());
        when(brandService.findAll()).thenReturn(brands);

        List<BrandDto> response = brandClientController.getBrands();

        assertEquals(brands, response);
        verify(brandService).findAll();
    }
}
