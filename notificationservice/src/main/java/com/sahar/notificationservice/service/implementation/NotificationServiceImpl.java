package com.sahar.notificationservice.service.implementation;

import com.sahar.notificationservice.model.Message;
import com.sahar.notificationservice.repository.NotificationRepository;
import com.sahar.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service // Marks this class as a service component, to be managed by Spring.
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;// This is the repository that handles the database operations related to notifications.

    @Override
    public Message sendMessage(String fromUserUuid, String toEmail, String subject, String message) {
        return notificationRepository.sendMessage(fromUserUuid, toEmail, subject, message);
        // Calls the notificationRepository to persist the message data in the database.
    }

    // Retrieves all messages for a specific user identified by userUuid.
    @Override
    public List<Message> getMessages(String userUuid) {
        var messages = notificationRepository.getMessages(userUuid);
        // For each message retrieved, it updates the message status (either 'SENT' or 'RECEIVED') based on the user's role (sender or receiver).
        messages.forEach(message ->
                message.setStatus(
                        notificationRepository.getMessageStatus(Objects.equals(userUuid, message.getSenderUuid()) ? message.getSenderUuid() : message.getReceiverUuid(), message.getMessageId())
                ));
        // Returns the list of messages with updated statuses.
        return messages;
    }

    // Retrieves all messages in a specific conversation for a user, identified by the conversationId.
    @Override
    public List<Message> getConversations(String userUuid, String conversationId) {
        var messages = notificationRepository.getConversations(userUuid, conversationId);
        // For each message in the conversation, updates its status to "READ".
        messages.forEach(message ->
                message.setStatus(
                        notificationRepository.updateMessageStatus(userUuid, message.getMessageId(), "READ")
                ));
        return messages;
    }
}