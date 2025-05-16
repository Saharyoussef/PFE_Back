package com.sahar.dashboardservice.event;
import com.sahar.dashboardservice.domain.Notification;
import com.sahar.dashboardservice.enumeration.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;


@Component
@RequiredArgsConstructor
@Slf4j
public class ApiEventListener {
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private static final String NOTIFICATION_TOPIC = "NOTIFICATION_TOPIC";

    @EventListener
    public void onApiEvent(Event event) {
        if (event.getEventType() == EventType.REPORT) {
            log.info("ApiEventListener received event: {}, preparing to send to Kafka.", event);
            try {
                Notification notificationPayload = new Notification(event);
                Message<Notification> message = MessageBuilder
                        .withPayload(notificationPayload)
                        .setHeader(TOPIC, NOTIFICATION_TOPIC)
                        .build();

                kafkaTemplate.send(message);
                log.info("Event sent to Kafka topic {}: {}", NOTIFICATION_TOPIC, message.getPayload());
            } catch (Exception e) {
                log.error("Error sending event to Kafka: {}", e.getMessage(), e);
            }
        } else {
            log.debug("ApiEventListener received non-report event, ignoring: {}", event.getEventType());
        }
    }
}