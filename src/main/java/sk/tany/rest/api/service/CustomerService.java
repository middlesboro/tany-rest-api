package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerDto save(CustomerDto customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream().map(customerMapper::toDto).toList();
    }

    public Page<CustomerDto> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDto);
    }

    public Optional<CustomerDto> findById(String id) {
        return customerRepository.findById(id).map(customerMapper::toDto);
    }

    public void deleteById(String id) {
        customerRepository.deleteById(id);
    }
}
