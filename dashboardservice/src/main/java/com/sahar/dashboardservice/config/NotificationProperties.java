package com.sahar.dashboardservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.notifications")
@Getter
@Setter
public class NotificationProperties {
    private Map<String, String> predefinedEmails = new HashMap<>();
}