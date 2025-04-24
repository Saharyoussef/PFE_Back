package com.sahar.authorizationserver.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.stereotype.Component;

//This AuthenticationManager will be injected wherever you need to perform manual authentication
@Component
@AllArgsConstructor
public class UserAuthenticationManager {
    private final UserAuthenticationProvider authenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider);
    }
}