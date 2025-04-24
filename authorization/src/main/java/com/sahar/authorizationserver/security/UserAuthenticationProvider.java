package com.sahar.authorizationserver.security;

import com.sahar.authorizationserver.exception.ApiException;
import com.sahar.authorizationserver.model.User;
import com.sahar.authorizationserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Component
@RequiredArgsConstructor
public class UserAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;  // Custom service to fetch user by email
    private final BCryptPasswordEncoder encoder; // Password encoder used to validate passwords

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            // 1. Get user from DB by email (authentication.getPrincipal() contains the email)
            var user = userService.getUserByEmail((String) authentication.getPrincipal());

            // 2. Validate account status (locked, disabled, expired)
            validateUser.accept(user);

            // 3. Check if provided password matches the stored encoded password
            if(encoder.matches((String) authentication.getCredentials(), user.getPassword())) {
                return authenticated(user, "[PROTECTED]", commaSeparatedStringToAuthorityList(user.getRole() + "," + user.getAuthorities()));
            } else throw new BadCredentialsException("Incorrect email/password. Please try again.");
        } catch (BadCredentialsException | ApiException | LockedException | CredentialsExpiredException |
                 DisabledException exception) {
            throw new ApiException(exception.getMessage());
        } catch (Exception exception) {
            throw new ApiException("Unable to authenticate. Please try again.");
        }
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        // This provider supports only UsernamePasswordAuthenticationToken type
        return authenticationType.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }

    // Lambda to validate user account status before authentication
    private final Consumer<User> validateUser = user -> {
        if(!user.isAccountNonLocked() || user.getLoginAttempts() >= 5) {
            throw new LockedException(String.format(user.getLoginAttempts() > 0 ? "Account currently locked after %s failed login attempts" : "Account currently locked", user.getLoginAttempts()));
        }
        if(!user.isEnabled()) {
            throw new DisabledException("Your account is currently disabled");
        }
        if(!user.isAccountNonExpired()) {
            throw new DisabledException("Your account has expired. Please contact administration");
        }
    };
}