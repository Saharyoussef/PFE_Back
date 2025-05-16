package com.sahar.dashboardservice.domain;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportEventData {
    private String recipientEmail;
    private String reportContentBase64; // PDF content as Base64 string
    private String reportFileName;
    private String userName; // Optional
    private String subject;  // Optional
}
