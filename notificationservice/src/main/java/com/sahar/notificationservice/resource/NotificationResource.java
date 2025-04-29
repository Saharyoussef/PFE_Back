package com.sahar.notificationservice.resource;

import com.sahar.notificationservice.domain.Response;
import com.sahar.notificationservice.utils.RequestUtils;
import com.sahar.notificationservice.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController // Marks this class as a REST Controller that handles HTTP requests and returns JSON
@AllArgsConstructor
@RequestMapping("/notification")
public class NotificationResource {
    private final NotificationService notificationService; // Injects the service that handles the business logic

    // === 1. SEND A NEW MESSAGE ===
    @PostMapping("/messages")
    public ResponseEntity<Response> sendMessage(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "toEmail") String toEmail, @RequestParam(value = "message") String message) {
        notificationService.sendMessage(authentication.getName(), toEmail, "Subject", message);
        var messages = notificationService.getMessages(authentication.getName());
        return created(URI.create("")).body(RequestUtils.getResponse(request, of("messages", messages), "Message created", CREATED));
    }

    // === 2. REPLY TO A MESSAGE ===
    @PostMapping("/reply")
    public ResponseEntity<Response> replyToMessage(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "toEmail") String toEmail, @RequestParam(value = "message") String message) {
        var newMessage = notificationService.sendMessage(authentication.getName(), toEmail, "Subject", message);
        return ok(RequestUtils.getResponse(request, of("message", newMessage), "Message sent", OK));
    }

    // === 3. GET ALL MESSAGES ===
    @GetMapping("/messages")
    public ResponseEntity<Response> getMessages(@NotNull Authentication authentication, HttpServletRequest request) {
        var messages = notificationService.getMessages(authentication.getName());
        return ok(RequestUtils.getResponse(request, of("messages", messages), "Message retrieved", OK));
    }

    // === 4. GET SPECIFIC CONVERSATION MESSAGES ===
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<Response> getConversations(@NotNull Authentication authentication, HttpServletRequest request, @PathVariable String conversationId) {
        var conversation = notificationService.getConversations(authentication.getName(), conversationId);
        var messages = notificationService.getMessages(authentication.getName());
        return ok(RequestUtils.getResponse(request, of("conversation", conversation, "messages", messages), "Message retrieved", OK));
    }

}