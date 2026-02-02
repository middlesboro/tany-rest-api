package sk.tany.rest.api.service.isklad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.dto.isklad.CreateBrandRequest;
import sk.tany.rest.api.dto.isklad.CreateNewOrderRequest;
import sk.tany.rest.api.dto.isklad.CreateSupplierRequest;
import sk.tany.rest.api.dto.isklad.ISkladAuth;
import sk.tany.rest.api.dto.isklad.ISkladRequest;
import sk.tany.rest.api.dto.isklad.ISkladResponse;
import sk.tany.rest.api.dto.isklad.InventoryDetailRequest;
import sk.tany.rest.api.dto.isklad.InventoryDetailResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class ISkladServiceImpl implements ISkladService {

    private final ISkladProperties iskladProperties;
    private final RestTemplate restTemplate;

    @Override
    public ISkladResponse<Object> createNewOrder(CreateNewOrderRequest request) {
        return sendRequest("CreateNewOrder", request, new ParameterizedTypeReference<ISkladResponse<Object>>() {});
    }

    @Override
    public ISkladResponse<Object> createBrand(CreateBrandRequest request) {
        return sendRequest("CreateProducer", request, new ParameterizedTypeReference<ISkladResponse<Object>>() {});
    }

    @Override
    public ISkladResponse<Object> createSupplier(CreateSupplierRequest request) {
        return sendRequest("CreateSupplier", request, new ParameterizedTypeReference<ISkladResponse<Object>>() {});
    }

    @Override
    public ISkladResponse<InventoryDetailResult> getInventory(InventoryDetailRequest request) {
        return sendRequest("InventoryDetail", request, new ParameterizedTypeReference<ISkladResponse<InventoryDetailResult>>() {});
    }

    private <Req, Res> ISkladResponse<Res> sendRequest(String method, Req data, ParameterizedTypeReference<ISkladResponse<Res>> responseType) {
        if (!iskladProperties.isEnabled()) {
            return null;
        }

        ISkladAuth auth = ISkladAuth.builder()
                .authId(iskladProperties.getAuthId())
                .authKey(iskladProperties.getAuthKey())
                .authToken(iskladProperties.getAuthToken())
                .build();

        ISkladRequest.RequestData<Req> reqData = ISkladRequest.RequestData.<Req>builder()
                .reqMethod(method)
                .reqData(data)
                .build();

        ISkladRequest<Req> requestWrapper = ISkladRequest.<Req>builder()
                .auth(auth)
                .request(reqData)
                .build();

        try {
//            ResponseEntity<ISkladResponse<Res>> response = restTemplate.exchange(
//                    iskladProperties.getUrl(),
//                    HttpMethod.POST,
//                    new HttpEntity<>(requestWrapper),
//                    responseType
//            );
//            return response.getBody();

            return null;
        } catch (Exception e) {
            log.error("Error calling iSklad API method: {}", method, e);
            throw new RuntimeException("Failed to call iSklad API", e);
        }
    }
}
