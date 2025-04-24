package com.sahar.authorizationserver.security;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

// Custom Authentication class that extends Spring Security's OAuth2ClientAuthenticationToken
// It is used specifically for refresh token authentication based on client ID without a secret
public class ClientRefreshTokenAuthentication extends OAuth2ClientAuthenticationToken {

    // Constructor used in the converter to create an unauthenticated token (before validation)
    public ClientRefreshTokenAuthentication(String clientId) {
        super(clientId, ClientAuthenticationMethod.NONE, null, null);
    }

    // Constructor used after the client is validated and resolved as a RegisteredClient
    public ClientRefreshTokenAuthentication(RegisteredClient registeredClient) {
        super(registeredClient, ClientAuthenticationMethod.NONE, null);
    }
}