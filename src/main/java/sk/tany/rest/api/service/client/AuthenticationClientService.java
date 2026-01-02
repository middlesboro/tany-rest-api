package sk.tany.rest.api.service.client;

public interface AuthenticationClientService {
    void initiateLogin(String email);
    String verifyAndGenerateCode(String token);
    String exchangeCode(String code);
}
