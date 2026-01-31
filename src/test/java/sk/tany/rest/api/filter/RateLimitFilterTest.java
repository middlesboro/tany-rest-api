package sk.tany.rest.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        rateLimitFilter = new RateLimitFilter();
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void doFilter_AllowRequest_WhenUnderLimit() throws ServletException, IOException {
        when(response.getStatus()).thenReturn(200);

        rateLimitFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_BlockRequest_WhenOverLimit() throws ServletException, IOException {
        when(response.getStatus()).thenReturn(200);

        // Consume all 100 permits
        for (int i = 0; i < 100; i++) {
            rateLimitFilter.doFilter(request, response, chain);
        }

        // Next one should be blocked
        rateLimitFilter.doFilter(request, response, chain);

        verify(response).setStatus(429);
        // The chain should have been called 100 times, not 101
        verify(chain, times(100)).doFilter(request, response);
    }

    @Test
    void doFilter_ApplyPenalty_When404() throws ServletException, IOException {
        // 1. Send 1 request that returns 404
        when(response.getStatus()).thenReturn(404);
        rateLimitFilter.doFilter(request, response, chain);

        // This should have consumed 1 (acquire) + 9 (penalty) = 10 permits.
        // Remaining = 90.

        // 2. Send 90 successful requests
        when(response.getStatus()).thenReturn(200);
        for (int i = 0; i < 90; i++) {
            rateLimitFilter.doFilter(request, response, chain);
        }

        // Total attempts: 1 + 90 = 91.
        // If penalty worked, we consumed 10 + 90 = 100.
        // So bucket is empty.

        // 3. Next request should fail
        rateLimitFilter.doFilter(request, response, chain);

        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    void doFilter_ApplyPenalty_When500() throws ServletException, IOException {
        // 1. Send 1 request that returns 500
        when(response.getStatus()).thenReturn(500);
        rateLimitFilter.doFilter(request, response, chain);

        // Consumed 10. Remaining 90.

        // 2. Send 90 successful requests
        when(response.getStatus()).thenReturn(200);
        for (int i = 0; i < 90; i++) {
            rateLimitFilter.doFilter(request, response, chain);
        }

        // 3. Next request should fail
        rateLimitFilter.doFilter(request, response, chain);

        verify(response, atLeastOnce()).setStatus(429);
    }
}
