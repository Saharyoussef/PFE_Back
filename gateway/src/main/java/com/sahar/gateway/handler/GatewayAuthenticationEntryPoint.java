package com.sahar.gateway.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.sahar.gateway.utils.RequestUtils.handleErrorResponse;

@Component
public class GatewayAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // This method is triggered when a user tries to access a protected resource
    // WITHOUT being authenticated (e.g., no token, expired token, or invalid token).
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        handleErrorResponse(request, response, exception);
    }
}
