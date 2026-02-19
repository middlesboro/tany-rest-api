package sk.tany.rest.api.component;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.exception.AuthenticationException;

@Component
public class SecurityUtil {

    public String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException.InvalidToken("User not authenticated");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwt = jwtAuthToken.getToken();
            return jwt.getClaimAsString("customerId");
        }

        if (authentication instanceof Jwt jwt) {
            return (String) jwt.getClaims().get("customerId");
        }

        return null;
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException.InvalidToken("User not authenticated");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwt = jwtAuthToken.getToken();
            return new User(jwt.getClaimAsString("customerId"), jwtAuthToken.getName());
        }

        if (authentication instanceof Jwt jwt) {
            return new User(jwt.getClaimAsString("customerId"), jwt.getSubject());
        }

        return null;
    }

    public record User(String userId, String email) {
    }

}
