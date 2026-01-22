package sk.tany.rest.api.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.service.common.AuthenticationService;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        authenticationService.initiateLogin(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        String authorizationCode = authenticationService.verifyAndGenerateCode(token);
        String redirectUrl = frontendUrl + "/authentication/success?authorizationCode=" + authorizationCode;
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @PostMapping("/exchange")
    public ResponseEntity<Map<String, String>> exchange(@RequestBody Map<String, String> request) {
        String authorizationCode = request.get("authorizationCode");
        if (authorizationCode == null || authorizationCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String jwt = authenticationService.exchangeCode(authorizationCode);
        return ResponseEntity.ok(Collections.singletonMap("token", jwt));
    }
}
