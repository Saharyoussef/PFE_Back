package com.sahar.discoveryservice.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DiscoveryAccessDeniedHandler implements AccessDeniedHandler {

    // This method is called when a user tries to access a resource they don't have permission for (403 error)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        var writer = response.getWriter();
        writer.println("HTTP Status 403 - " + exception.getMessage());
        // Optional: you could send a full HTML response instead of plain text

        //response.setContentType("TEXT/HTML");
        /*writer.println("""
                <html>
                <head>
                </head>
                </html>
                """);*/
    }
}

