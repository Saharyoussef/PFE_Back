package com.sahar.discoveryservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.sahar.discoveryservice.constants.Roles.*;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class FilterChainConfig {
    private final DiscoveryUserDetailsService userDetailsService;

    // Define the security filter chain for the application
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/eureka/**")) // Disable CSRF protection for Eureka internal endpoints
                        .userDetailsService(userDetailsService) // Use the custom userDetailsService for authentication
                .exceptionHandling(exception -> exception.accessDeniedHandler(new DiscoveryAccessDeniedHandler()))
                // Define authorization rules for different URL patterns
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to static resources under these paths
                        .requestMatchers("/eureka/fonts/**","/eureka/css/**","/eureka/js/**","/eureka/images/**","/icon/**").permitAll()

                        // Require APP_READ authority to access any Eureka endpoint
                        .requestMatchers("/eureka/**").hasAnyAuthority(APP_READ)

                        // Require APP_READ authority to access any other path
                        .requestMatchers("/**").hasAnyAuthority(APP_READ)
                        .anyRequest().authenticated())

                // Use HTTP Basic authentication with a custom entry point (login behavior)
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new DiscoveryAuthenticationEntryPoint()));
        return http.build();
    }
}
