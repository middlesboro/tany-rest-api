package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;
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
}
