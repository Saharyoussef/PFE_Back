package com.sahar.notificationservice.repository.implementation;

import com.sahar.notificationservice.model.Message;
import com.sahar.notificationservice.query.MessageQuery;
import com.sahar.notificationservice.utils.NotificationUtils;
import com.sahar.notificationservice.exception.ApiException;
import com.sahar.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Map.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {
    private final JdbcClient jdbc;

    @Override
    public Message sendMessage(String fromUserUuid, String toEmail, String subject, String message) {
        try {
            return jdbc.sql(MessageQuery.CREATE_MESSAGE_FUNCTION).params(of("messageUuid", NotificationUtils.randomUUID.get(), "fromUserUuid", fromUserUuid, "toEmail", toEmail, "subject", subject, "message", message, "conversationId", conversationExist(fromUserUuid, toEmail) ? getConversationId(fromUserUuid, toEmail) : NotificationUtils.randomUUID.get())).query(Message.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", toEmail));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public List<Message> getMessages(String userUuid) {
        try {
            return jdbc.sql(MessageQuery.SELECT_MESSAGES_QUERY).params(of("userUuid", userUuid)).query(Message.class).list();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public List<Message> getConversations(String userUuid, String conversationId) {
        try {
            return jdbc.sql(MessageQuery.SELECT_MESSAGES_BY_CONVERSATION_ID_QUERY).params(of("userUuid", userUuid)).query(Message.class).list();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public String getMessageStatus(String userUuid, Long messageId) {
        try {
            return jdbc.sql(MessageQuery.SELECT_MESSAGE_STATUS_QUERY).params(of("userUuid", userUuid, "messageId", messageId)).query(String.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public String updateMessageStatus(String userUuid, Long messageId, String status) {
        try {
            jdbc.sql(MessageQuery.UPDATE_MESSAGE_STATUS_QUERY).params(of("userUuid", userUuid, "messageId", messageId, "status", status)).update();
            return status;
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private Boolean conversationExist(String userUuid, String toEmail) {
        try {
            var count = jdbc.sql(MessageQuery.SELECT_MESSAGE_COUNT_QUERY).params(of("userUuid", userUuid, "toEmail", toEmail)).query(Integer.class).single();
            return count > 0;
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private String getConversationId(String userUuid, String toEmail) {
        try {
            return jdbc.sql(MessageQuery.SELECT_CONVERSATION_ID_QUERY).params(of("userUuid", userUuid, "toEmail", toEmail)).query(String.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No user found user by UUID %s", userUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }
}