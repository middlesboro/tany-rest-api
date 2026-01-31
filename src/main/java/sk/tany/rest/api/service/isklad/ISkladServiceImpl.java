package sk.tany.rest.api.service.isklad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.dto.isklad.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ISkladServiceImpl implements ISkladService {

    private final ISkladProperties iskladProperties;
    private final RestTemplate restTemplate;

    @Override
    public ISkladResponse<Object> createNewOrder(CreateNewOrderRequest request) {
        return sendRequest("CreateNewOrder", request);
    }

    @Override
    public ISkladResponse<Object> createProducer(CreateProducerRequest request) {
        return sendRequest("CreateProducer", request);
    }

    @Override
    public ISkladResponse<Object> createSupplier(CreateSupplierRequest request) {
        return sendRequest("CreateSupplier", request);
    }

    private <T> ISkladResponse<Object> sendRequest(String method, T data) {
        ISkladAuth auth = ISkladAuth.builder()
                .authId(iskladProperties.getAuthId())
                .authKey(iskladProperties.getAuthKey())
                .authToken(iskladProperties.getAuthToken())
                .build();

        ISkladRequest.RequestData<T> reqData = ISkladRequest.RequestData.<T>builder()
                .reqMethod(method)
                .reqData(data)
                .build();

        ISkladRequest<T> requestWrapper = ISkladRequest.<T>builder()
                .auth(auth)
                .request(reqData)
                .build();

        try {
            ResponseEntity<ISkladResponse<Object>> response = restTemplate.exchange(
                    iskladProperties.getUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(requestWrapper),
                    new ParameterizedTypeReference<ISkladResponse<Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling iSklad API method: {}", method, e);
            throw new RuntimeException("Failed to call iSklad API", e);
        }
    }
}
