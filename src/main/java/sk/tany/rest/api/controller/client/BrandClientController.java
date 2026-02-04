package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.client.BrandClientService;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandClientController {

    private final BrandClientService brandService;

    @GetMapping
    public Page<BrandDto> getBrands(@PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return brandService.findAll(pageable);
    }
}
