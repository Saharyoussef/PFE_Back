package com.sahar.authorizationserver.security;

import com.sahar.authorizationserver.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;

// This class handles successful authentication with support for MFA (Multi-Factor Authentication)
public class MfaAuthenticationHandler implements AuthenticationSuccessHandler {
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final AuthenticationSuccessHandler mfaNotEnabled = new SavedRequestAwareAuthenticationSuccessHandler();
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final String authority; // The required authority (e.g. "MFA_AUTHENTICATED") to mark the user as MFA-authenticated

    // Constructor to set the redirect URL and the required MFA authority
    public MfaAuthenticationHandler(String successUrl, String authority) {
        // Creates a success handler that always redirects to a specific URL (e.g., /home)
        SimpleUrlAuthenticationSuccessHandler authenticationSuccessHandler = new SimpleUrlAuthenticationSuccessHandler(successUrl);
        authenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true); // Always go to successUrl, not saved requests
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authority = authority;
    }

    // Called when authentication succeeds
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Only handle Username/Password authentication (not JWT or other tokens)
        if(authentication instanceof UsernamePasswordAuthenticationToken) {
            var user = (User) authentication.getPrincipal();
            if(!user.isMfa()) {
                mfaNotEnabled.onAuthenticationSuccess(request, response, authentication);
            } else {
                saveAuthentication(request, response, new MfaAuthentication(authentication, authority));
                authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }

    // Save the modified Authentication object (with MFA authority) to the SecurityContext and session
    private void saveAuthentication(HttpServletRequest request, HttpServletResponse response, MfaAuthentication authentication) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext(); // Create a new, empty security context
        securityContext.setAuthentication(authentication); // Set the new MFA authentication (which includes the original user details and extra MFA role)
        SecurityContextHolder.setContext(securityContext); // Update the context for the current thread
        securityContextRepository.saveContext(securityContext, request, response);// Save the security context to the session using the repository
    }
}