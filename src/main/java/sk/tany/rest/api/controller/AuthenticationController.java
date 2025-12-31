package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.service.AuthenticationService;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

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
    public ResponseEntity<Map<String, String>> verify(@RequestParam String token) {
        try {
            String sessionToken = authenticationService.verifyLogin(token);
            return ResponseEntity.ok(Collections.singletonMap("token", sessionToken));
        } catch (sk.tany.rest.api.exception.InvalidTokenException e) {
            return ResponseEntity.status(401).build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
