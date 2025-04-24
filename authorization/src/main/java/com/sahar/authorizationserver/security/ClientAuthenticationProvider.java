package com.sahar.authorizationserver.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;


//This class validates OAuth2 clients (like the Angular app) when they try to use the refresh token flow.
@Component
@AllArgsConstructor
public class ClientAuthenticationProvider implements AuthenticationProvider {
    private final RegisteredClientRepository registeredClientRepository; // Repository that contains all registered OAuth2 clients (like the Angular app)

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var clientRefreshTokenAuthentication = (ClientRefreshTokenAuthentication) authentication;

        // Validate that the client is using the correct authentication method
        if(!ClientAuthenticationMethod.NONE.equals(clientRefreshTokenAuthentication.getClientAuthenticationMethod())){
            // If it's using something other than NONE (which means no authentication), reject it
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Authentication method is not valid", null));
        }
        var clientId = clientRefreshTokenAuthentication.getPrincipal().toString();
        var registeredClient = registeredClientRepository.findByClientId(clientId);

        // If no such client exists, throw an error
        if(registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Client is not valid", null));
        }

        // If the client doesn't support the authentication method being used, throw error
        if(!registeredClient.getClientAuthenticationMethods().contains(clientRefreshTokenAuthentication.getClientAuthenticationMethod())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Authentication method is not valid", null));
        }

        // If all checks pass, return a new ClientRefreshTokenAuthentication with the valid registered client
        return new ClientRefreshTokenAuthentication(registeredClient);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientRefreshTokenAuthentication.class.isAssignableFrom(authentication);
    }
}