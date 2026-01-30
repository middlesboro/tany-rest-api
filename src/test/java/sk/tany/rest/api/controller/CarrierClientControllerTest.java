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
import sk.tany.rest.api.controller.client.CarrierClientController;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.service.client.CarrierClientService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CarrierClientControllerTest {

    @Mock
    private CarrierClientService carrierService;

    @InjectMocks
    private CarrierClientController carrierClientController;

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

        Page<CarrierDto> result = carrierClientController.getCarriers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Carrier", result.getContent().getFirst().getName());
        verify(carrierService, times(1)).findAll(pageable);
    }
}
