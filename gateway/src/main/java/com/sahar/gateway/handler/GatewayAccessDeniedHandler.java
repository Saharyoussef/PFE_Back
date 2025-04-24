package com.sahar.gateway.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.sahar.gateway.utils.RequestUtils.handleErrorResponse;

@Component
public class GatewayAccessDeniedHandler implements AccessDeniedHandler {

    // This method is called automatically by Spring Security when an authenticated user
    // tries to access a resource they are NOT authorized to access (403 Forbidden).
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        handleErrorResponse(request, response, exception);
    }
}
