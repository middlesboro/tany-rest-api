package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.service.CustomerService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'GUEST')")
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/context")
    public ResponseEntity<CustomerContextDto> getCustomerContext(@RequestParam(required = false) String cartId) {
        return ResponseEntity.ok(customerService.getCustomerContext(cartId));
    }
}
