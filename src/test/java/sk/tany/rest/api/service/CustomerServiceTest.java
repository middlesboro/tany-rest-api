package sk.tany.rest.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.mapper.CustomerMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void save() {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setPassword("password");
        Customer customer = new Customer();
        customer.setPassword("password");

        when(customerMapper.toEntity(customerDto)).thenReturn(customer);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        CustomerDto result = customerService.save(customerDto);

        assertEquals(customerDto, result);
        verify(customerRepository, times(1)).save(customer);
        verify(passwordEncoder, times(1)).encode("password");
    }

    @Test
    void findAll() {
        when(customerRepository.findAll()).thenReturn(Collections.singletonList(new Customer()));
        when(customerMapper.toDto(any(Customer.class))).thenReturn(new CustomerDto());

        assertEquals(1, customerService.findAll().size());
    }

    @Test
    void findById() {
        Customer customer = new Customer();
        customer.setId("1");
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId("1");
        when(customerRepository.findById("1")).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        Optional<CustomerDto> result = customerService.findById("1");

        assertEquals("1", result.get().getId());
    }

    @Test
    void deleteById() {
        customerService.deleteById("1");
        verify(customerRepository, times(1)).deleteById("1");
    }
}
