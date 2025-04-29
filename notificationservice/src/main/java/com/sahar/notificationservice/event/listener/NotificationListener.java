package com.sahar.notificationservice.event.listener;

import com.fasterxml.jackson.databind.ObjectMapper; // Used for converting JSON-like data into Java objects
import com.sahar.notificationservice.domain.Data;
import com.sahar.notificationservice.domain.Notification;
import com.sahar.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {
    private static final String NOTIFICATION_TOPIC = "NOTIFICATION_TOPIC"; // Name of the Kafka topic to listen to
    private final EmailService emailService;

    @KafkaListener(topics = NOTIFICATION_TOPIC)
    public void handleNotification(Notification notification) {
        log.info("Received notification: {}", notification.toString());
        var mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        var data = mapper.convertValue(notification.getPayload().getData(), Data.class);
        // Based on the type of event, call the appropriate email sending method
        switch (notification.getPayload().getEventType()) {
            case RESETPASSWORD -> emailService.sendPasswordResetHtmlEmail(data.getName(), data.getEmail(), data.getToken());
            case USER_CREATED -> emailService.sendNewAccountHtmlEmail(data.getName(), data.getEmail(), data.getToken());
        }
    }
}