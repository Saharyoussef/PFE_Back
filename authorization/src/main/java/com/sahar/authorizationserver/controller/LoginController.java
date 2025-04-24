package com.sahar.authorizationserver.controller;

import com.sahar.authorizationserver.exception.ApiException;
import com.sahar.authorizationserver.model.User;
import com.sahar.authorizationserver.security.MfaAuthentication;
import com.sahar.authorizationserver.service.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static com.sahar.authorizationserver.utils.RequestUtils.getMessage;
import static com.sahar.authorizationserver.utils.UserUtils.getUser;


@Controller
@AllArgsConstructor
public class LoginController {
    // The securityContextRepository is responsible for handling the saving and retrieval of the security context,
    // which stores the user's authentication information during their session.
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final AuthenticationFailureHandler authenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler("/mfa?error");
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }


    /**
     * Displays the MFA page with the authenticated user's email.
     * param model The model object to add attributes to the view.
     * param context The current security context that contains authentication details.
     * return the MFA view.
     */
    @GetMapping("/mfa")
    public String mfa(Model model, @CurrentSecurityContext SecurityContext context) {
        model.addAttribute("email", getAuthenticatedUser(context.getAuthentication()));
        return "mfa";
    }

    /**
     * Validates the MFA code entered by the user. If valid, proceeds with authentication.
     * If invalid, redirects back with an error.
     * param code The MFA code entered by the user.
     * param request The HTTP request object.
     * param response The HTTP response object.
     * param context The current security context.
     */
    @PostMapping("/mfa")
    public void validateCode(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response, @CurrentSecurityContext SecurityContext context) throws ServletException, IOException {
        var user = getUser(context.getAuthentication());
        if(userService.verifyQrCode(user.getUserUuid(), code)) {
            this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, getAuthentication(request, response));
            return;
        }
        this.authenticationFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("Invalid QR code. Please try again."));
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    /**
     * Handles errors and displays an appropriate error page with a message.
     * param request The HTTP request object.
     * param response The HTTP response object.
     * param model The model object to add attributes to the view.
     * param exception The exception that triggered the error page.
     * return The name of the error view.
     */
    @GetMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response, Model model, Exception exception) {
        var errorException = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if(errorException instanceof ApiException || errorException instanceof BadCredentialsException) {
            request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorException);
            return "login";
        }
        model.addAttribute("message", getMessage(request));
        return "error";
    }

    /**
     * Retrieves the authentication object from the current security context and sets it in the session.
     * This method is used during MFA verification to update the authentication context.
     * param request The HTTP request object.
     * param response The HTTP response object.
     * return The primary authentication object (without MFA).
     */
    private Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext securityContext = SecurityContextHolder.getContext(); // Retrieves the current security context.
        MfaAuthentication mfaAuthentication = (MfaAuthentication) securityContext.getAuthentication(); // Extracts the MFA authentication.
        securityContext.setAuthentication(mfaAuthentication); // Sets the updated authentication in the context.
        SecurityContextHolder.setContext(securityContext); // Updates the security context holder.
        securityContextRepository.saveContext(securityContext, request, response);// Saves the updated context to the session.
        return mfaAuthentication.getPrimaryAuthentication();
    }

    /**
     * Retrieves the authenticated user's email from the authentication object.
     * param authentication The authentication object that holds user details.
     * return The email of the authenticated user.
     */
    private Object getAuthenticatedUser(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getEmail();
    }

}