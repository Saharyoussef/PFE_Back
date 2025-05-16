package com.sahar.notificationservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportEventData {
    private String recipientEmail;
    private String reportContentBase64; // PDF content as Base64 string
    private String reportFileName;
    private String userName; // Optional: for personalizing the email
    private String subject;  // Optional: for the email subject
}