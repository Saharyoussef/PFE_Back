package com.sahar.dashboardservice.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
    private static final Pattern UID_PATTERN = Pattern.compile("/d/([a-zA-Z0-9]+)/");

    public static String extractUidFromUrl(String grafanaUrl) {
        Matcher matcher = UID_PATTERN.matcher(grafanaUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid Grafana URL: UID not found");
    }
}
