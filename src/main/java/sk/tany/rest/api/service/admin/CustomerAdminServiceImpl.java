package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerAdminServiceImpl implements CustomerAdminService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Page<CustomerDto> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDto);
    }

    @Override
    public Optional<CustomerDto> findById(String id) {
        return customerRepository.findById(id).map(customerMapper::toDto);
    }

    @Override
    public CustomerDto save(CustomerDto customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    public CustomerDto patch(String id, sk.tany.rest.api.dto.admin.customer.patch.CustomerPatchRequest patchDto) {
        var customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
        customerMapper.updateEntityFromPatch(patchDto, customer);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    public void deleteById(String id) {
        customerRepository.deleteById(id);
    }
}
