package sk.tany.rest.api.service.isklad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.dto.isklad.CreateNewOrderRequest;
import sk.tany.rest.api.dto.isklad.CreateProducerRequest;
import sk.tany.rest.api.dto.isklad.CreateSupplierRequest;
import sk.tany.rest.api.dto.isklad.ISkladResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ISkladServiceImplTest {

    @Mock
    private ISkladProperties iskladProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ISkladServiceImpl iSkladService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(iskladProperties.getUrl()).thenReturn("http://api.isklad.com");
        when(iskladProperties.getAuthId()).thenReturn("testId");
        when(iskladProperties.getAuthKey()).thenReturn("testKey");
        when(iskladProperties.getAuthToken()).thenReturn("testToken");
    }

    @Test
    void createNewOrder() {
        CreateNewOrderRequest request = CreateNewOrderRequest.builder().customerName("John").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        when(restTemplate.exchange(
                eq("http://api.isklad.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        ISkladResponse<Object> response = iSkladService.createNewOrder(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
    }

    @Test
    void createProducer() {
        CreateProducerRequest request = CreateProducerRequest.builder().name("Producer").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        when(restTemplate.exchange(
                eq("http://api.isklad.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        ISkladResponse<Object> response = iSkladService.createProducer(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
    }

    @Test
    void createSupplier() {
        CreateSupplierRequest request = CreateSupplierRequest.builder().name("Supplier").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        when(restTemplate.exchange(
                eq("http://api.isklad.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        ISkladResponse<Object> response = iSkladService.createSupplier(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
    }
}
