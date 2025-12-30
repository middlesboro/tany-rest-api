package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.service.CustomerService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCustomers_ShouldReturnPagedCustomers() {
        Pageable pageable = PageRequest.of(0, 10);
        CustomerDto customerDto = new CustomerDto();
        customerDto.setFirstname("Test");
        customerDto.setLastname("Customer");
        Page<CustomerDto> customerPage = new PageImpl<>(Collections.singletonList(customerDto));

        when(customerService.findAll(pageable)).thenReturn(customerPage);

        Page<CustomerDto> result = customerController.getAllCustomers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test", result.getContent().get(0).getFirstname());
        verify(customerService, times(1)).findAll(pageable);
    }
}
