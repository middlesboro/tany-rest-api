package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.controller.admin.CarrierAdminController;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.service.admin.CarrierAdminService;
import sk.tany.rest.api.service.common.ImageService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CarrierAdminControllerTest {

    @Mock
    private CarrierAdminService carrierService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private CarrierAdminController carrierAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCarriers_ShouldReturnPagedCarriers() {
        Pageable pageable = PageRequest.of(0, 10);
        CarrierDto carrierDto = new CarrierDto();
        carrierDto.setName("Test Carrier");
        Page<CarrierDto> carrierPage = new PageImpl<>(Collections.singletonList(carrierDto));

        when(carrierService.findAll(pageable)).thenReturn(carrierPage);

        Page<CarrierDto> result = carrierAdminController.getCarriers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Carrier", result.getContent().get(0).getName());
        verify(carrierService, times(1)).findAll(pageable);
    }

    @Test
    void createCarrier_ShouldReturnCreatedCarrier() {
        CarrierDto carrierDto = new CarrierDto();
        carrierDto.setName("New Carrier");
        when(carrierService.save(carrierDto)).thenReturn(carrierDto);

        ResponseEntity<CarrierDto> result = carrierAdminController.createCarrier(carrierDto);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(carrierDto, result.getBody());
    }

    @Test
    void getCarrier_ShouldReturnCarrier_WhenFound() {
        String id = "1";
        CarrierDto carrierDto = new CarrierDto();
        carrierDto.setId(id);
        when(carrierService.findById(id)).thenReturn(Optional.of(carrierDto));

        ResponseEntity<CarrierDto> result = carrierAdminController.getCarrier(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(carrierDto, result.getBody());
    }

    @Test
    void updateCarrier_ShouldReturnUpdatedCarrier() {
        String id = "1";
        CarrierDto carrierDto = new CarrierDto();
        when(carrierService.update(id, carrierDto)).thenReturn(carrierDto);

        ResponseEntity<CarrierDto> result = carrierAdminController.updateCarrier(id, carrierDto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(carrierDto, result.getBody());
    }

    @Test
    void deleteCarrier_ShouldReturnNoContent() {
        String id = "1";
        ResponseEntity<Void> result = carrierAdminController.deleteCarrier(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(carrierService, times(1)).deleteById(id);
    }
}
