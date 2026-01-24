package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.component.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @GetMapping("/context")
    public ResponseEntity<CustomerClientGetResponse> getCustomerContext(@RequestParam(required = false) String cartId) {
        CustomerContextDto customerContext = customerService.getCustomerContext(cartId);
        return ResponseEntity.ok(customerClientApiMapper.toGetResponse(customerContext));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping
    public ResponseEntity<CustomerClientDetailResponse> getCustomer() {
        CustomerDto customerDto = customerService.getCurrentCustomer();
        return ResponseEntity.ok(customerClientApiMapper.toDetailResponse(customerDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @PutMapping
    public ResponseEntity<CustomerClientUpdateResponse> updateCustomer(
            @RequestBody CustomerClientUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        CustomerDto customerDto = customerClientApiMapper.toDto(request);
        CustomerDto currentCustomer = customerService.getCurrentCustomer();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tokenCustomerId = jwtUtil.extractClaim(token, claims -> claims.get("customerId", String.class));
            if (tokenCustomerId != null && customerDto.getId() != null && !customerDto.getId().equals(tokenCustomerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update another customer's data");
            }
        }

        if (customerDto.getId() != null && !customerDto.getId().equals(currentCustomer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update another customer's data");
        }
        CustomerDto updatedCustomer = customerService.updateCustomer(customerDto);
        return ResponseEntity.ok(customerClientApiMapper.toUpdateResponse(updatedCustomer));
    }
}
