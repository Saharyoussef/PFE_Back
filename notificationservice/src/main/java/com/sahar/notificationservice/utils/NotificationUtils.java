package com.sahar.notificationservice.utils;

import java.util.UUID;
import java.util.function.Supplier;

public class NotificationUtils {

    public static Supplier<String> randomUUID = () -> UUID.randomUUID().toString();

    public static String getVerificationUrl(String host, String token) {
        return host + "/verify/account?token=" + token;
    }

    public static String getResetPasswordUrl(String host, String token) {
        return host + "/verify/password?token=" + token;
    }
}