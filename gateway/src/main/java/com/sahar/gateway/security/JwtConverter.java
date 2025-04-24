package com.sahar.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Component
public class JwtConverter implements Converter<Jwt, JwtAuthenticationToken> {
    private static final String AUTHORITY_KEY = "authorities";

    // This method is called to convert a raw Jwt token into an authenticated Spring Security object
    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        // Get the "authorities" claim from the JWT (as a String)
        // Example claim: "ROLE_USER,ROLE_ADMIN"
        var claims = (String) jwt.getClaims().get(AUTHORITY_KEY);

        // Convert the comma-separated string into a list of GrantedAuthority objects
        // This is what Spring Security uses to check roles/permissions
        var authorities = commaSeparatedStringToAuthorityList(claims);

        // Create and return a JwtAuthenticationToken using:
        // - the original JWT
        // - the extracted authorities
        // - the subject (user identifier, typically the username or user ID)
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
