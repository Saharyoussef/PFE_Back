package com.sahar.notificationservice.service;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {
    void sendNewAccountHtmlEmail(String name, String to, String token);
    void sendPasswordResetHtmlEmail(String name, String to, String token);
    @Async
    void sendReportEmailWithAttachment(String recipientEmail, String subject, String userName, byte[] pdfAttachment, String attachmentFilename);

}