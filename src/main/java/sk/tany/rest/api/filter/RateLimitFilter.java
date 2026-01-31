package sk.tany.rest.api.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter implements Filter {

    private final Cache<String, RateLimiter> limiters;

    public RateLimitFilter() {
        this.limiters = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);
        RateLimiter rateLimiter = limiters.get(clientIp, this::createRateLimiter);

        boolean permitted = rateLimiter.acquirePermission(1);
        if (!permitted) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Too many requests");
            return;
        }

        chain.doFilter(request, response);

        int status = httpResponse.getStatus();
        if (status == 404 || status == 500) {
            // Apply penalty: consume 9 more permits (total cost 10)
            // Since timeout is 0, acquirePermission won't wait. We drain up to 9 permits.
            for (int i = 0; i < 9; i++) {
                if (!rateLimiter.acquirePermission(1)) {
                    break;
                }
            }
        }
    }

    private RateLimiter createRateLimiter(String ip) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ZERO) // Fail immediately if not available
                .build();
        return RateLimiter.of(ip, config);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
