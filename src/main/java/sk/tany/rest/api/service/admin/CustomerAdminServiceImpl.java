package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerAdminServiceImpl implements CustomerAdminService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<CustomerDto> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDto);
    }

    @Override
    public Page<CustomerDto> search(String firstname, String lastname, String email, String phone, Pageable pageable) {
        Query query = new Query();

        if (firstname != null) {
            query.addCriteria(Criteria.where("firstname").is(firstname));
        }
        if (lastname != null) {
            query.addCriteria(Criteria.where("lastname").is(lastname));
        }
        if (email != null) {
            query.addCriteria(Criteria.where("email").is(email));
        }
        if (phone != null) {
            query.addCriteria(Criteria.where("phone").is(phone));
        }

        long count = mongoTemplate.count(query, Customer.class);
        query.with(pageable);
        List<Customer> customers = mongoTemplate.find(query, Customer.class);

        return new PageImpl<>(
                customers.stream().map(customerMapper::toDto).collect(Collectors.toList()),
                pageable,
                count
        );
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
