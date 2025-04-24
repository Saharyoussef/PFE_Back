package com.sahar.authorizationserver.security;

import lombok.Getter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@Getter
public class MfaAuthentication extends AnonymousAuthenticationToken {
    private final Authentication primaryAuthentication;

    /**
     * Constructor for the MFA Authentication Token.
     * It wraps the original authentication and adds a special authority to indicate that MFA is required.
     * param authentication the original authentication (usually after username/password login)
     * param authority a custom authority (e.g. "MFA_REQUIRED") used to trigger MFA handling
     */
    public MfaAuthentication(Authentication authentication, String authority) {
        super("anonymous", "anonymous", createAuthorityList("ROLE_ANONYMOUS", authority));
        this.primaryAuthentication = authentication;
    }

    /**
     * Override getPrincipal to return the real authenticated user,instead of the default "anonymous" string.
     * This way,we can still access the actual user who logged in during MFA steps.
     */
    @Override
    public Object getPrincipal() {
        return this.primaryAuthentication;
    }
}