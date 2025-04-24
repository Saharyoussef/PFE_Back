package com.sahar.authorizationserver.security;

import lombok.NoArgsConstructor;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Base64;


@Component
@NoArgsConstructor
public class ClientOAuth2RefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {
    private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding());

    /**
     * This method generates a refresh token based on the OAuth2TokenContext.
     * The token will only be generated if the context specifically asks for a REFRESH_TOKEN.
     */
    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        if(!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        } else {
            var issueAt = Instant.now();
            // The expiration time of the refresh token — based on the access token TTL from the client settings
            var expiresAt = issueAt.plus(context.getRegisteredClient().getTokenSettings().getAccessTokenTimeToLive());
            return new OAuth2RefreshToken(refreshTokenGenerator.generateKey(), issueAt, expiresAt);
        }
    }
}