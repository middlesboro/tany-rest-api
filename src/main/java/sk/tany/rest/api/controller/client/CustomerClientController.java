package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientDetailResponse;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateRequest;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateResponse;
import sk.tany.rest.api.mapper.CustomerClientApiMapper;
import sk.tany.rest.api.service.client.CustomerClientService;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerClientController {

    private final CustomerClientService customerService;
    private final CustomerClientApiMapper customerClientApiMapper;

    @GetMapping("/context")
    public ResponseEntity<CustomerClientGetResponse> getCustomerContext(@RequestParam(required = false) String cartId) {
        CustomerContextDto customerContext = customerService.getCustomerContext(cartId);
        return ResponseEntity.ok(customerClientApiMapper.toGetResponse(customerContext));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<CustomerClientDetailResponse> getCustomer() {
        CustomerDto customerDto = customerService.getCurrentCustomer();
        return ResponseEntity.ok(customerClientApiMapper.toDetailResponse(customerDto));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @PutMapping
    public ResponseEntity<CustomerClientUpdateResponse> updateCustomer(@RequestBody CustomerClientUpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
        CustomerDto customerDto = customerClientApiMapper.toDto(request);
        customerDto.setId(jwt.getClaimAsString("customerId"));

        CustomerDto updatedCustomer = customerService.updateCustomer(customerDto);
        return ResponseEntity.ok(customerClientApiMapper.toUpdateResponse(updatedCustomer));
    }
}
