package com.sahar.userservice.utils;

import com.sahar.userservice.exception.ApiException;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.sahar.userservice.constant.Constants.WEVIOO;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public class UserUtils {

    // Supplier for generating a random UUID (Universal Unique Identifier).
    public static Supplier<String> randomUUUID = () -> UUID.randomUUID().toString();

    // Supplier for generating a "member ID" in the format: XXXX-XX-XXXX, where X is a random numeric value.
    public static Supplier<String> memberId = () -> randomNumeric(4) + "-" + randomNumeric(2) + "-" + randomNumeric(4);

    // Function that creates a QrData object based on a provided secret key (qrCodeSecret).
    // The QrData is used to configure the QR code generation.
    public static Function<String, QrData> qrDataFunction = qrCodeSecret -> new QrData.Builder()
            .issuer(WEVIOO)  // Issuer of the QR code, could be your app or service name
            .label(WEVIOO) // Label to display in the QR code (e.g., application name)
            .secret(qrCodeSecret) // Secret key used for generating the QR code
            .algorithm(HashingAlgorithm.SHA1)  // Hashing algorithm (SHA1 is commonly used for QR-based 2FA)
            .digits(6) // Number of digits in the generated code (common for OTP)
            .period(30) // Period in seconds between code changes (e.g., 30 seconds for OTP)
            .build();  // Build the QrData object with the above settings.

    // Function that generates a QR code image URI based on a given secret key.
    // It uses the qrDataFunction to configure the QR code data and a generator to create the QR code image.
    public static Function<String, String> qrCodeImageUri = qrCodeSecret -> {
        try {
            var data = qrDataFunction.apply(qrCodeSecret);
            var generator = new ZxingPngQrGenerator();
            var imageData = generator.generate(data);
            return getDataUriForImage(imageData, generator.getImageMimeType());
        } catch (QrGenerationException exception) {
            throw new ApiException(exception.getMessage());
        }
    };

    // This generates a random secret key for use in generating QR codes.
    public static Supplier<String> qrCodeSecret = () -> new DefaultSecretGenerator().generate();
}