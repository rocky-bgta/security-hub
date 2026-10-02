package com.example.contextdemo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtRevocationService jwtRevocationService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            JwtRevocationService jwtRevocationService) {
        this.jwtService = jwtService;
        this.jwtRevocationService = jwtRevocationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            JwtPrincipal principal = jwtService.parse(token);

            if (jwtRevocationService.isRevoked(principal.jwtId())) {
                writeUnauthorized(response, "JWT has been revoked. Please login again.");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of());

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            request.setAttribute("rawJwt", token);

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            writeUnauthorized(response, "Invalid or expired JWT. Please login again.");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\""
                        + escaped + "\"}");
    }
}
