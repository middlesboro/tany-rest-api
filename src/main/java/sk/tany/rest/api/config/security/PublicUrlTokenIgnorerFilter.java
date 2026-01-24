package sk.tany.rest.api.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sk.tany.rest.api.config.SecurityProperties;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PublicUrlTokenIgnorerFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // if is a public URL, remove Authorization header
        if (isPublicUrl(request)) {
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return null;
                    }
                    return super.getHeader(name);
                }
            };
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicUrl(HttpServletRequest request) {
        List<RequestMatcher> publicMatchers = securityProperties.getExcludedUrls().stream()
                .map(url -> {
                    String[] parts = url.split(" ");
                    if (parts.length > 1) {
                        return new AntPathRequestMatcher(parts[1], parts[0]);
                    } else {
                        return new AntPathRequestMatcher(parts[0]);
                    }
                })
                .collect(Collectors.toList());

        return publicMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }

}