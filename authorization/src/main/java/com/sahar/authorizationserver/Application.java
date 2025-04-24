package com.sahar.authorizationserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.AuthorizationGrantType; // OAuth2: Enum representing supported grant types (e.g., authorization_code, refresh_token, client_credentials, etc.)
import org.springframework.security.oauth2.core.ClientAuthenticationMethod; // OAuth2: Enum representing how clients authenticate (e.g., client_secret_basic, none, etc.)
import org.springframework.security.oauth2.core.oidc.OidcScopes; // OIDC (OpenID Connect): Constants for standard scopes like "openid", "profile", "email"
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient; // Represents a client application (like your Angular frontend) registered with the Authorization Server
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository; // Interface for saving and loading RegisteredClient objects (usually from DB)
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings; // Contains settings related to token lifetime (access token, refresh token, etc.)

import java.time.Duration;

import static java.util.UUID.randomUUID;

@Slf4j // Lombok annotation – auto-generates a logger named 'log' for logging
@SpringBootApplication
@EnableDiscoveryClient // Enables Eureka Client for service discovery
public class Application {
	@Value("${ui.app.url}") // Injects the UI redirect URI from application.yml
	private String redirectUri;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ApplicationRunner runner(RegisteredClientRepository registeredClientRepository) {
		return args -> {
			// Checks if a client with clientId "client" already exists in the DB
			RegisteredClient client = registeredClientRepository.findByClientId("client");
			// If not found, register a new OAuth2 client
			if(client == null) {
				try {
					// Create a new RegisteredClient using the builder pattern
					var registerdClient = RegisteredClient.withId(randomUUID().toString())
							.clientId("client").clientSecret("secret")
							.clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // No client authentication (PKCE in browser apps)
							// Supported grant types: Authorization Code + Refresh Token
							.authorizationGrantTypes(types -> {
								types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
								types.add(AuthorizationGrantType.REFRESH_TOKEN);
							})
							// Allowed scopes for this client (OpenID Connect related)
							.scopes(scopes -> {
										scopes.add(OidcScopes.OPENID); // Required for ID tokens
										scopes.add(OidcScopes.PROFILE); // Access to user profile info
										scopes.add(OidcScopes.EMAIL); // Access to user's email
									}
							)
							// Redirect URI (after successful login) – typically the Angular app's callback
							.redirectUri(redirectUri)
							// Redirect after logout – optional but used for user experience
							.postLogoutRedirectUri("http://127.0.0.1:8083")
							.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
							// Token settings – custom expiration durations
							.tokenSettings(TokenSettings.builder().refreshTokenTimeToLive(Duration.ofDays(90))
									.accessTokenTimeToLive(Duration.ofDays(1)).build()).build();
					// Save the client to the database
					registeredClientRepository.save(registerdClient);
				} catch (Exception exception) {
					log.error(exception.getMessage());
				}
			}
		};
	}

}