package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.client.BrandClientService;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandClientController {

    private final BrandClientService brandService;

    @GetMapping
    public List<BrandDto> getBrands() {
        return brandService.findAll();
    }
}
