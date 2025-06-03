package com.sahar.gateway.security;

import com.sahar.gateway.handler.GatewayAccessDeniedHandler;
import com.sahar.gateway.handler.GatewayAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpMethod.OPTIONS;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ResourceServerConfig {
    // Injects the JWKS URI from the application properties (used to validate JWTs)
    @Value("${jwks.uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configure authorization rules
                .authorizeHttpRequests( authorize -> authorize
                        // Public endpoints that don't require authentication
                        .requestMatchers("/user/register/**", "/user/verify/account/**", "/user/verify/password/**", "/user/resetpassword/**", "/user/image/**", "/authorization/**", "/.well-known/**","/dashboard/screenshots_data/**").permitAll()
                        // Any other endpoint requires authentication
                        .anyRequest().authenticated())

                // Set up the application as an OAuth2 resource server with JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .accessDeniedHandler(new GatewayAccessDeniedHandler())
                        .authenticationEntryPoint(new GatewayAuthenticationEntryPoint())
                        // Set the JWKS endpoint (public keys used to validate tokens)
                        .jwt(jwt -> jwt.jwkSetUri(jwkSetUri)
                                // Use a custom converter to convert the JWT into Authentication object
                                .jwtAuthenticationConverter(new JwtConverter())));
        return http.build();
    }

    // CORS Configuration Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var corsConfiguration = new CorsConfiguration();
        // Allow credentials like cookies or authorization headers
        corsConfiguration.setAllowCredentials(true);
        // Allowed origins (frontend apps making requests)
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3001", "http://localhost:4200"));
        // Headers allowed in requests
        corsConfiguration.setAllowedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X-Requested-With", ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        // Headers exposed in the response
        corsConfiguration.setExposedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X-Requested-With", ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        // Allowed HTTP methods
        corsConfiguration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), PATCH.name(), DELETE.name(), OPTIONS.name()));
        // How long the CORS settings are cached by the browser (in seconds)
        corsConfiguration.setMaxAge(3600L);
        // Register the CORS config for all endpoints
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}