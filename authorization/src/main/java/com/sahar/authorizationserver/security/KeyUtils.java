package com.sahar.authorizationserver.security;

import com.nimbusds.jose.jwk.RSAKey;
import com.sahar.authorizationserver.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;


@Slf4j
@Component // Makes this class a Spring Bean so Spring can manage it and inject it where needed
public class KeyUtils {
    private static final String RSA = "RSA"; // Name of algorithm used for key generation

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${keys.private}")
    private String privateKey;

    @Value("${keys.public}")
    private String publicKey;

    public RSAKey getRSAKeyPair() {
        return generateRSAKeyPair(privateKey, publicKey);
    }

    private RSAKey generateRSAKeyPair(String privateKeyName, String publicKeyName) {
        KeyPair keyPair;

        // Define the path to the 'keys' directory inside src/main/resources
        var keysDirectory = Paths.get("src", "main", "resources", "keys");
        // Ensure the keys directory exists (creates it if not)
        verifyKeysDirectory(keysDirectory);

        // CASE 1: If both private and public key files already exist, we load them
        if(Files.exists(keysDirectory.resolve(privateKeyName)) && Files.exists(keysDirectory.resolve(publicKeyName))) {
            log.info("RSA keys already exist. Loading keys from file paths: {}, {}", publicKeyName, privateKeyName);
            try {
                // Load the key files
                var privateKeyFile = keysDirectory.resolve(privateKeyName).toFile();
                var publicKeyFile = keysDirectory.resolve(publicKeyName).toFile();

                // Create RSA KeyFactory to convert bytes to Key objects
                var keyFactory = KeyFactory.getInstance(RSA);

                // Load the public key bytes and reconstruct it
                byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

                // Load the private key bytes and reconstruct it
                byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

                // Assign a fixed key ID (could be randomized with UUID)
                var keyId = "25efbae8-0b1b-42f8-873d-93cac9f20648"; //UUID.randomUUID().toString();
                log.info("Key ID: {}", keyId);

                // Return a new RSAKey (used for signing/verifying JWTs)
                return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException(exception.getMessage());
            }
        } else {
            // If in production and keys don't exist, throw an error (we shouldn't generate keys in prod)
            if(activeProfile.equalsIgnoreCase("prod")) {
                throw new ApiException("Public and private keys don't exist in prod environment");
            }
        }

        // CASE 2: If keys don't exist, generate a new pair
        log.info("Generating new public and private keys: {}, {}", publicKeyName, privateKeyName);
        try {
            // Generate a new RSA key pair (2048 bits)
            var keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

            // Extract private and public keys from the generated pair
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            // Save the private key to file
            try(var fos = new FileOutputStream(keysDirectory.resolve(privateKeyName).toFile())) {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
                fos.write(keySpec.getEncoded());
            }

            // Save the public key to file
            try(var fos = new FileOutputStream(keysDirectory.resolve(publicKeyName).toFile())) {
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
                fos.write(keySpec.getEncoded());
            }

            // Assign a new random UUID as key ID
            var keyId = "25efbae8-0b1b-42f8-873d-93cac9f20648"; //UUID.randomUUID().toString();
            log.info("Key ID: {}", keyId);

            // Return the newly generated RSAKey
            return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
            } catch (Exception exception) {
                throw new ApiException(exception.getMessage());
            }
    }

    private void verifyKeysDirectory(Path keysDirectory) {
        if(!Files.exists(keysDirectory)) {
            try {
                Files.createDirectories(keysDirectory);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException(exception.getMessage());
            }
            log.info("Created keys directory: {}", keysDirectory);
        }
    }
}