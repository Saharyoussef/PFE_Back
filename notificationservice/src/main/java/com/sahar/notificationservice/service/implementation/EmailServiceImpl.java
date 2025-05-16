package com.sahar.notificationservice.service.implementation;

import com.sahar.notificationservice.service.EmailService;
import com.sahar.notificationservice.utils.NotificationUtils;
import com.sahar.notificationservice.exception.ApiException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    // Watch this video to know how to send emails using Spring Boot: https://youtu.be/onCzCxDyR24?si=pjtZeysRH4I723Zr

    // Constants for email subjects and template names
    public static final String NEW_USER_ACCOUNT_VERIFICATION = "New Account Verification";
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String ACCOUNT_VERIFICATION_TEMPLATE = "newaccount";
    public static final String PASSWORD_RESET_TEMPLATE = "resetpassword";
    public static final String PASSWORD_RESET_REQUEST = "Password Reset Request";
    public static final String REPORT_EMAIL_TEMPLATE = "reportemail"; // report template

    // Dependency injection of JavaMailSender for sending emails, and TemplateEngine for processing email templates.
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    // Values fetched from application.properties/yml file to set host and fromEmail (sender email address).
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    // Method to send a "New Account Verification" email asynchronously.
    @Override
    @Async // Marks this method as asynchronous, meaning it will run in a separate thread and not block the caller.
    public void sendNewAccountHtmlEmail(String name, String to, String token) {
        try {
            // Create a context object for the email template
            var context = new Context();

            // Set variables in the context that will be replaced in the template (name and verification URL with token)
            context.setVariables(Map.of("name", name, "url", NotificationUtils.getVerificationUrl(host, token)));
            var text = templateEngine.process(ACCOUNT_VERIFICATION_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);

            // Create a helper to add more email attributes (subject, recipient, content, etc.)
            helper.setPriority(1);
            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }

    // Method to send a "Password Reset Request" email asynchronously.
    @Override
    @Async
    public void sendPasswordResetHtmlEmail(String name, String to, String token) {
        try {
            var context = new Context();
            context.setVariables(Map.of("name", name, "url", NotificationUtils.getResetPasswordUrl(host, token)));
            var text = templateEngine.process(PASSWORD_RESET_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(PASSWORD_RESET_REQUEST);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(text, true);
            emailSender.send(message);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Unable to send email");
        }
    }
    @Override
    @Async
    public void sendReportEmailWithAttachment(String recipientEmail, String subject, String userName, byte[] pdfAttachment, String attachmentFilename) {
        try {
            var context = new Context();
            context.setVariables(Map.of("name", userName, "reportName", attachmentFilename));
            var text = templateEngine.process(REPORT_EMAIL_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            // true = multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);

            helper.setPriority(1);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setText(text, true); // true = HTML

            // Add attachment
            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfAttachment));

            emailSender.send(message);
            log.info("Report email sent successfully to {} with attachment {}", recipientEmail, attachmentFilename);

        } catch (Exception exception) {
            log.error("Error sending report email to {}: {}", recipientEmail, exception.getMessage(), exception);
            throw new ApiException("Unable to send report email with attachment");
        }
    }

    // Helper method to create a new MimeMessage instance from the emailSender.
    private MimeMessage getMimeMessage() {
        return emailSender.createMimeMessage();
    }
}