package sk.tany.rest.api.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.exception.AuthenticationException;

@Slf4j
@Component
public class SecurityUtil {

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

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

    public String getLoggedInUserIdSafe() {
        try {
            return getLoggedInUserId();
        } catch (Exception e) {
            log.info("Error in get logged user id: ", e);
            return null;
        }
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
