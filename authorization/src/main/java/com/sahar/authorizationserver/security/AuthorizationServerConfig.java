package com.sahar.authorizationserver.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order; // Spring: Allows you to set execution order of filters (SecurityFilterChain for example)
import org.springframework.http.MediaType; // Spring Web: Used to match requests based on media type (e.g., HTML vs JSON)
import org.springframework.jdbc.core.JdbcTemplate; // Spring JDBC: Utility to execute SQL operations using JDBC
import org.springframework.security.config.Customizer; // Spring Security: Allows you to customize security settings using the HttpSecurity object
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Spring Security: Used to configure HTTP security (URL access rules, filters, etc.)
import org.springframework.security.core.GrantedAuthority; // Spring Security: Represents a user's authority/role (e.g., ROLE_USER, ADMIN)
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token; // OAuth2: Base interface for access/refresh tokens
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder; // JWT support (Nimbus is the underlying library used by Spring for JWT encoding)
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository; // OAuth2: Stores registered clients in a database via JDBC (Postgres, MySQL, etc.)
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository; // OAuth2: Interface to manage client registrations (you inject this to access/save clients)
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator; // OAuth2: Token generator that supports multiple types (access token, refresh token, etc.)
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext; // OAuth2: Context used when creating JWTs (e.g., you can customize claims here)
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer; // OAuth2: Lets you customize how tokens (especially JWTs) are created (e.g., adding custom claims)
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain; // Spring Security: Defines a custom security filter chain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint; // Spring Security: Redirects to login page for unauthenticated requests (mainly for browser apps)
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler; // Spring Security: Redirects to the original URL after successful login
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler; // Spring Security: Redirects to failure page on login failure
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler; // Spring Security: Deletes session cookies on logout
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher; // Spring Security: Matches requests based on media type (used to return different error pages depending on request type)
import org.springframework.web.cors.CorsConfiguration; // CORS support: Define which domains can talk to this API (needed for frontend-backend interaction)
import org.springframework.web.cors.CorsConfigurationSource; // CORS source object: Maps CORS configuration to specific URLs
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // CORS configuration manager for path patterns

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.*; // Static import of common HTTP header names (used in CORS settings)
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN;

@Slf4j
@Configuration
@RequiredArgsConstructor // Lombok: generates constructor for all final fields
public class AuthorizationServerConfig {
    // http://localhost:8080/.well-known/openid-configuration
    private static final String AUTHORITY_KEY = "authorities";
    private final JwtConfiguration jwtConfiguration; // Custom class to load JWT keys (public/private)

    // ========= [1] MAIN SECURITY FILTER CHAIN FOR AUTH SERVER ================
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2ServerConfig(HttpSecurity http, RegisteredClientRepository registeredClientRepository, OAuth2TokenCustomizer<JwtEncodingContext> customizer) throws Exception {
        // Enable CORS for frontend access
        http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()));
        // Set up the OAuth2 Authorization Server endpoints
        var authorizationConfig = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
                .securityMatcher(authorizationConfig.getEndpointsMatcher()) // Only apply to OAuth2 endpoints (e.g. /oauth2/token)
                .with(authorizationConfig, authorizationServer -> authorizationServer
                                .oidc(Customizer.withDefaults()) // Enable OpenID Connect support
                                .authorizationServerSettings(authorizationServerSettings())
                                .registeredClientRepository(registeredClientRepository) // Where clients are stored (DB in this case)
                                .tokenGenerator(tokenGenerator()) // JWT + refresh token generator
                                .clientAuthentication(authentication -> {
                                    // Custom way to authenticate clients (for refresh token flow)
                                    authentication.authenticationConverter(new ClientRefreshTokenAuthenticationConverter());
                                    authentication.authenticationProvider(new ClientAuthenticationProvider(registeredClientRepository));
                        }))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());  // All requests must be authenticated
        http.exceptionHandling(exceptions -> exceptions.accessDeniedPage("/accessdenied")
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    // ========= [2] SECOND SECURITY FILTER CHAIN FOR LOGIN/MFA ETC ================
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()));
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/oauth2/authorize/**", "oauth2/authorize").permitAll()
                .requestMatchers(POST, "/logout").permitAll()
                .requestMatchers("/mfa").hasAuthority("MFA_REQUIRED")
                .anyRequest().authenticated());
        http.formLogin(login -> login
                .loginPage("/login")
                .successHandler(new MfaAuthenticationHandler("/mfa", "MFA_REQUIRED"))
                .failureHandler(new SimpleUrlAuthenticationFailureHandler("/login")));
        http.logout(logout -> logout.logoutSuccessUrl("http://localhost:3001")
                .addLogoutHandler(new CookieClearingLogoutHandler("JSESSIONID"))); // Clear session cookie
        return http.build();
    }

    // ========= [3] TOKEN GENERATOR CONFIG (JWT + REFRESH TOKEN) ================
    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        var jwtGenerator = UserJwtGenerator.init(new NimbusJwtEncoder((jwtConfiguration.jwkSource()))); // Uses private/public key to sign JWTs
        jwtGenerator.setJwtCustomizer(customizer()); // Inject custom claims (e.g. authorities)
        OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenOAuth2TokenGenerator = new ClientOAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenOAuth2TokenGenerator);
    }

    // ========= [4] HANDLER USED ON LOGIN SUCCESS ================
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler(); // Standard Spring Security login success handler
    }

    // ========= [5] OAUTH2 SERVER SETTINGS ================
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build(); // Default settings (issuer URL, endpoints, etc.)
    }

    // ========= [6] CUSTOM CLAIMS ADDED TO JWT TOKEN ================
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> customizer() {
        return context -> {
            if(ACCESS_TOKEN.equals(context.getTokenType())) {
                // Inject user's authorities into JWT claims
                context.getClaims().claims(claims -> claims.put(AUTHORITY_KEY, getAuthorities(context)));
            }
        };
    }

    // ========= [7] CLIENTS STORED IN DB WITH JDBC ================
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);// Load/store OAuth2 clients from database
    }

    // ========= [8] CORS CONFIG FOR FRONTEND COMMUNICATION ================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        //These are the whitelisted frontend origins allowed to call your API.
        //It includes local IPs for different dev machines, and Angular dev ports
        corsConfiguration.setAllowedOrigins(List.of("http://192.168.1.157:3001", "http://localhost:3001", "http://192.168.1.159:3001", "http://localhost:4200", "100.14.214.212:3001",  "http://localhost:4200", "http://localhost:3001", "http://192.168.1.216:3001"));
        //These are the HTTP headers your backend allows the frontend to send.
        corsConfiguration.setAllowedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X_REQUESTED_WITH", ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        //These are the headers that the browser is allowed to read in the response.
        corsConfiguration.setExposedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X_REQUESTED_WITH", ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        //HTTP methods are allowed from cross-origin requests.
        corsConfiguration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), PATCH.name(), DELETE.name(), OPTIONS.name()));
        corsConfiguration.setMaxAge(3600L); // Browser will cache the CORS preflight response for 3600 seconds (1 hour), reducing extra OPTIONS calls.
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);  // Apply to all paths in the backend
        return source;
    }

    // ========= [9] UTILITY: EXTRACT AUTHORITIES FROM JWT CONTEXT ================
    private String getAuthorities(JwtEncodingContext context) {
        return context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
    }
}