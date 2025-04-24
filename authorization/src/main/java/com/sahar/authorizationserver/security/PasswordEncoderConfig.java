package com.sahar.authorizationserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



@Configuration
public class PasswordEncoderConfig {
    // Defines the strength (or "work factor") of the BCrypt algorithm.
    // Higher values make password hashing more secure but also more computationally expensive.
    public static final int STRENGTH = 12;

    //This encoder will be used to hash passwords (e.g. during registration or authentication).
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder(STRENGTH);
    }
}