package com.sahar.authorizationserver.utils;

import com.sahar.authorizationserver.model.User;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;

public class UserUtils {
    /**
     * Verifies a TOTP code (from a QR code scanner app like Google Authenticator)
     * using a shared secret.
     * param secret The user's TOTP secret key (e.g., from QR code setup)
     * param code   The current code the user entered (usually 6 digits)
     * return true if valid for the current time window, false otherwise
     */
    public static boolean verifyQrCode(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(); // Generates the expected 6-digit TOTP code
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider); // The verifier compares expected vs. actual codes
        return verifier.isValidCode(secret, code); // Check if the code is valid for the given secret
    }

    /**
     * Retrieves the custom User object from the Authentication context.
     * This supports both standard and OAuth2 flows.
     * param authentication The Spring Security Authentication object
     * return User The currently authenticated user
     */
    public static User getUser(Authentication authentication) {
        if(authentication instanceof OAuth2AuthorizationCodeRequestAuthenticationToken) {
            var usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication.getPrincipal();
                return (User) usernamePasswordAuthenticationToken.getPrincipal();
        }
        return (User) authentication.getPrincipal();
    }
}