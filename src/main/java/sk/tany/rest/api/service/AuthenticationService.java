package sk.tany.rest.api.service;

public interface AuthenticationService {
    void initiateLogin(String email);
    String verifyLogin(String token);
}
