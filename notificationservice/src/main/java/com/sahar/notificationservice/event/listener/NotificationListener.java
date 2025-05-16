package com.sahar.notificationservice.event.listener;

import com.fasterxml.jackson.databind.ObjectMapper; // Used for converting JSON-like data into Java objects
import com.sahar.notificationservice.domain.Data;
import com.sahar.notificationservice.domain.Notification;
import com.sahar.notificationservice.domain.ReportEventData;
import com.sahar.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Base64;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {
    private static final String NOTIFICATION_TOPIC = "NOTIFICATION_TOPIC";
    private final EmailService emailService;

    @KafkaListener(topics = NOTIFICATION_TOPIC)
    public void handleNotification(Notification notification) {
        log.info("Received notification: {}", notification.toString());
        var mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        switch (notification.getPayload().getEventType()) {
            case RESETPASSWORD: {
                var data = mapper.convertValue(notification.getPayload().getData(), Data.class);
                emailService.sendPasswordResetHtmlEmail(data.getName(), data.getEmail(), data.getToken());
                break;
            }
            case USER_CREATED: {
                var data = mapper.convertValue(notification.getPayload().getData(), Data.class);
                emailService.sendNewAccountHtmlEmail(data.getName(), data.getEmail(), data.getToken());
                break;
            }
            case REPORT: {
                try {
                    ReportEventData reportData = mapper.convertValue(notification.getPayload().getData(), ReportEventData.class);
                    byte[] pdfBytes = Base64.getDecoder().decode(reportData.getReportContentBase64());

                    String subject = reportData.getSubject() != null ? reportData.getSubject() : "Your Requested Report";
                    String userName = reportData.getUserName() != null ? reportData.getUserName() : "User";

                    emailService.sendReportEmailWithAttachment(
                            reportData.getRecipientEmail(),
                            subject,
                            userName,
                            pdfBytes,
                            reportData.getReportFileName()
                    );
                    log.info("Successfully processed REPORT event for email: {}", reportData.getRecipientEmail());
                } catch (Exception e) {
                    log.error("Error processing REPORT event: {}", e.getMessage(), e);
                }
                break;
            }
            default:
                log.warn("Received unhandled event type: {}", notification.getPayload().getEventType());
        }
    }
}