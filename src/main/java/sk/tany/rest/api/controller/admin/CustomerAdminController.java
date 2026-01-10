package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.admin.customer.create.CustomerAdminCreateRequest;
import sk.tany.rest.api.dto.admin.customer.create.CustomerAdminCreateResponse;
import sk.tany.rest.api.dto.admin.customer.get.CustomerAdminGetResponse;
import sk.tany.rest.api.dto.admin.customer.list.CustomerAdminListResponse;
import sk.tany.rest.api.dto.admin.customer.patch.CustomerPatchRequest;
import sk.tany.rest.api.dto.admin.customer.update.CustomerAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.customer.update.CustomerAdminUpdateResponse;
import sk.tany.rest.api.mapper.CustomerAdminApiMapper;
import sk.tany.rest.api.service.admin.CustomerAdminService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class CustomerAdminController {

    private final CustomerAdminService customerService;
    private final CustomerAdminApiMapper customerAdminApiMapper;

    @PostMapping
    public ResponseEntity<CustomerAdminCreateResponse> createCustomer(@RequestBody CustomerAdminCreateRequest customerDto) {
        CustomerDto savedCustomer = customerService.save(customerAdminApiMapper.toDto(customerDto));
        return new ResponseEntity<>(customerAdminApiMapper.toCreateResponse(savedCustomer), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<CustomerAdminListResponse> getAllCustomers(Pageable pageable) {
        return customerService.findAll(pageable)
                .map(customerAdminApiMapper::toListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerAdminGetResponse> getCustomerById(@PathVariable String id) {
        return customerService.findById(id)
                .map(customerAdminApiMapper::toGetResponse)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerAdminUpdateResponse> updateCustomer(@PathVariable String id, @RequestBody CustomerAdminUpdateRequest customerDto) {
        CustomerDto dto = customerAdminApiMapper.toDto(customerDto);
        dto.setId(id);
        CustomerDto updatedCustomer = customerService.save(dto);
        return new ResponseEntity<>(customerAdminApiMapper.toUpdateResponse(updatedCustomer), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerAdminUpdateResponse> patchCustomer(@PathVariable String id, @RequestBody CustomerPatchRequest patchDto) {
        CustomerDto updatedCustomer = customerService.patch(id, patchDto);
        return new ResponseEntity<>(customerAdminApiMapper.toUpdateResponse(updatedCustomer), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
