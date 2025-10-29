import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${security.cors.enable-strict:false}")
    private boolean corsEnableStrict;

    @Value("${security.cors.header-token:__cors_token_change_me__}")
    private String corsHeaderToken;

    @Override
    protected void doFilterInternal(@NotNull final HttpServletRequest request,
            @NotNull final HttpServletResponse response,
            @NotNull final FilterChain filterChain) throws ServletException, IOException {

        if (corsEnableStrict) {
            filterChain.doFilter(request, response);
        } else {
            final var apiKey = request.getHeader("X-API-Key");

            if (apiKey != null && apiKey.equals(corsHeaderToken)) {
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            }
        }
    }
}