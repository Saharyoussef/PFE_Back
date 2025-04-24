package com.sahar.authorizationserver.utils;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus; //This is an enum provided by Spring that represents standard HTTP status codes.

// Utility class for handling HTTP request-related operations
public class RequestUtils {

    // This method returns a custom error message based on the HTTP status code from the request
    public static String getMessage(HttpServletRequest request) {
        var status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if(null != status) {
            int statusCode = Integer.parseInt(status.toString());
            // Check if it's a 404 error (Not Found)
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return String.format("%s - Not Found error", statusCode);
            }
            // Check if it's a 500 error (Internal Server Error)
            if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return String.format("%s - Internal server error", statusCode);
            }
            // Check if it's a 403 error (Forbidden)
            if(statusCode == HttpStatus.FORBIDDEN.value()) {
                return String.format("%s - Forbidden error", statusCode);
            }
        }
        return "An error occurred";
    }
}