package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public ResponseEntity<CustomerClientDetailResponse> getCustomer() {
        CustomerDto customerDto = customerService.getCurrentCustomer();
        return ResponseEntity.ok(customerClientApiMapper.toDetailResponse(customerDto));
    }

    @PutMapping
    public ResponseEntity<CustomerClientUpdateResponse> updateCustomer(@RequestBody CustomerClientUpdateRequest request) {
        CustomerDto customerDto = customerClientApiMapper.toDto(request);
        CustomerDto updatedCustomer = customerService.updateCustomer(customerDto);
        return ResponseEntity.ok(customerClientApiMapper.toUpdateResponse(updatedCustomer));
    }
}
