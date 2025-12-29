package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerDto customerDto) {
        CustomerDto savedCustomer = customerService.save(customerDto);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CustomerDto> getAllCustomers() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable String id) {
        return customerService.findById(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable String id, @RequestBody CustomerDto customerDto) {
        customerDto.setId(id);
        CustomerDto updatedCustomer = customerService.save(customerDto);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
