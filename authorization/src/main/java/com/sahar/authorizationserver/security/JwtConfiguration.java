package com.sahar.authorizationserver.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;


@Configuration
@RequiredArgsConstructor
public class JwtConfiguration {
    private final KeyUtils keyUtils;

    /**
     * Bean for decoding JWT tokens (used by Authorization Server to validate incoming tokens)
     */
    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        // Build a JWT decoder using the public RSA key (used to verify JWT signatures)
        return NimbusJwtDecoder.withPublicKey(keyUtils.getRSAKeyPair().toRSAPublicKey()).build();
    }

    /**
     * Bean that provides a JWKSource (JSON Web Key Source) — this is used by Spring Security’s Authorization Server
     * to publish the public key as part of its discovery endpoint (/.well-known/jwks.json)
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // Get RSAKey (includes both public & private keys and a key ID)
        RSAKey rSaKey = keyUtils.getRSAKeyPair();

        // Create a JWKSet (a set of public keys) from the RSAKey
        JWKSet set = new JWKSet(rSaKey);

        // Return a JWKSource — this lambda receives a selector `j` and security context `sc`
        // It returns the matching keys from the set (here, just one key for now)
        return (j, sc) -> j.select(set);
    }
}