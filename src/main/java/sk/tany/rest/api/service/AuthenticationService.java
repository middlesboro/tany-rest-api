package sk.tany.rest.api.service;

public interface AuthenticationService {
    void initiateLogin(String email);
    String verifyAndGenerateCode(String token);
    String exchangeCode(String code);
}
