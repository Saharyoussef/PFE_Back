package com.sahar.userservice.event;

import com.sahar.userservice.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;
@Component
@RequiredArgsConstructor
public class ApiEventListener {
    // KafkaTemplate is a Spring Kafka class for sending messages to Kafka topics
    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private static final String NOTIFICATION_TOPIC = "NOTIFICATION_TOPIC";

    // This method is triggered when an event is published in the application
    // Build a Kafka message with the payload being a new Notification created from the event
    @EventListener
    public void onApiEvent(Event event) {
        var message = MessageBuilder.withPayload(new Notification(event)).setHeader(TOPIC, NOTIFICATION_TOPIC).build();
        kafkaTemplate.send(message);
    }
}