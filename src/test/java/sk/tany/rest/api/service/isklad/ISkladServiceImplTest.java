package sk.tany.rest.api.service.isklad;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.dto.isklad.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ISkladServiceImplTest {

    @Mock
    private ISkladProperties iskladProperties;

    private ISkladServiceImpl iSkladService;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(iskladProperties.getUrl()).thenReturn("http://api.isklad.com");
        when(iskladProperties.getAuthId()).thenReturn("testId");
        when(iskladProperties.getAuthKey()).thenReturn("testKey");
        when(iskladProperties.getAuthToken()).thenReturn("testToken");
        when(iskladProperties.isEnabled()).thenReturn(true);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        iSkladService = new ISkladServiceImpl(iskladProperties, builder.build());
    }

    @Test
    void createNewOrder() throws Exception {
        CreateNewOrderRequest request = CreateNewOrderRequest.builder().customerName("John").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        mockServer.expect(requestTo("http://api.isklad.com"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        ISkladResponse<Object> response = iSkladService.createNewOrder(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
        mockServer.verify();
    }

    @Test
    void createBrand() throws Exception {
        CreateBrandRequest request = CreateBrandRequest.builder().name("Producer").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        mockServer.expect(requestTo("http://api.isklad.com"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        ISkladResponse<Object> response = iSkladService.createBrand(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
        mockServer.verify();
    }

    @Test
    void createSupplier() throws Exception {
        CreateSupplierRequest request = CreateSupplierRequest.builder().name("Supplier").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        mockServer.expect(requestTo("http://api.isklad.com"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        ISkladResponse<Object> response = iSkladService.createSupplier(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
        mockServer.verify();
    }

    @Test
    void createOrUpdateProduct() throws Exception {
        UpdateInventoryCardRequest request = UpdateInventoryCardRequest.builder().itemId(123L).name("Product").build();
        ISkladResponse<Object> mockResponse = new ISkladResponse<>();
        mockResponse.setAuthStatus(200);

        when(iskladProperties.getShopSettingId()).thenReturn(1);

        mockServer.expect(requestTo("http://api.isklad.com"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        ISkladResponse<Object> response = iSkladService.createOrUpdateProduct(request);

        assertNotNull(response);
        assertEquals(200, response.getAuthStatus());
        assertEquals(1, request.getShopSettingId());
        mockServer.verify();
    }
}
