package sk.tany.rest.api.component;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.exception.AuthenticationException;

@Component
public class SecurityUtil {

    public String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException.InvalidToken("User not authenticated");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return (String) jwt.getClaims().get("customerId");
    }

}
