package com.sahar.notificationservice.service;

public interface EmailService {
    void sendNewAccountHtmlEmail(String name, String to, String token);
    void sendPasswordResetHtmlEmail(String name, String to, String token);

}