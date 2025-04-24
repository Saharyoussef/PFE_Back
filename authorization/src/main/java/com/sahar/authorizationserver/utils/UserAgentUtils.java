package com.sahar.authorizationserver.utils;

import com.sahar.authorizationserver.domain.Analyzer;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;

public class UserAgentUtils {
    // Constants for common HTTP headers
    private static final String USER_AGENT_HEADER = "user-agent";  // Contains info about the browser and OS
    private static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR"; // Used to get the real IP when behind a proxy/load balancer

    /**
     * Extracts the device name (e.g., "Android", "iPhone", "Windows") from the User-Agent header.
     */
    public static String getDevice(HttpServletRequest request) {
        var uaa = Analyzer.getInstance();
        var agent = uaa.parse(request.getHeader(USER_AGENT_HEADER));
        return agent.getValue(UserAgent.DEVICE_NAME);

    }

    /**
     * Extracts the client/browser name (e.g., "Chrome", "Firefox") from the User-Agent header.
     */
    public static String getClient(HttpServletRequest request) {
        var uaa = Analyzer.getInstance();
        var agent = uaa.parse(request.getHeader(USER_AGENT_HEADER));
        return agent.getValue(UserAgent.AGENT_NAME);
    }

    /**
     * Gets the user's IP address from the request.
     * It first checks the 'X-FORWARDED-FOR' header in case the app is behind a proxy/load balancer.
     * If not found, it uses the remote address directly from the request.
     */
    public static String getIpAddress(HttpServletRequest request) {
        var ipAddress = "Unknown IP";
        if(request != null) {
            ipAddress = request.getHeader(X_FORWARDED_FOR_HEADER);
            if(ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }
}