package com.CustomerManagmentApp.Customer_Managment_System.Security;

import com.CustomerManagmentApp.Customer_Managment_System.DTOs.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class ApiTokenFilter extends OncePerRequestFilter {

    @Value("${api.security.token}")
    private String expectedToken;

    private final ObjectMapper objectMapper;

    public ApiTokenFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header from {}", request.getRemoteAddr());
            sendUnauthorized(response, "Missing or malformed Authorization header. " +
                    "Expected: Authorization: Bearer <token>");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        if (!expectedToken.equals(token)) {
            log.warn("Invalid API token provided from {}", request.getRemoteAddr());
            sendUnauthorized(response, "Invalid API token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                message
        );
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
