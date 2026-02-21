package sk.tany.rest.api.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MagicLinkLoginFilter extends OncePerRequestFilter {

    private final MagicLinkAuthenticationProvider authenticationProvider;
    private final SecurityContextRepository securityContextRepository;
    private final RequestMatcher requestMatcher = new RegexRequestMatcher("/oauth2/authorize", null);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getParameter("token");
        if (token != null && !token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                MagicLinkAuthenticationToken authRequest = new MagicLinkAuthenticationToken(token);
                Authentication authResult = authenticationProvider.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authResult);
                securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
