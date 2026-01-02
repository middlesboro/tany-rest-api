package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.service.client.CarrierClientService;

@RestController
@RequestMapping("/api/carriers")
@RequiredArgsConstructor
public class CarrierClientController {

    private final CarrierClientService carrierService;

    @GetMapping
    public Page<CarrierDto> getCarriers(Pageable pageable) {
        return carrierService.findAll(pageable);
    }
}
