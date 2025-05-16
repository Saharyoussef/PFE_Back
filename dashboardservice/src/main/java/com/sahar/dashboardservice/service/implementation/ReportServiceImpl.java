package com.sahar.dashboardservice.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahar.dashboardservice.config.NotificationProperties;
import com.sahar.dashboardservice.domain.ReportEventData;
import com.sahar.dashboardservice.dtorequest.SendReportRequest;
import com.sahar.dashboardservice.dtoresponse.AnomalyApiResponse;
import com.sahar.dashboardservice.enumeration.EventType;
import com.sahar.dashboardservice.event.Event;
import com.sahar.dashboardservice.repository.ScreenshotDataRepository;
import com.sahar.dashboardservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ScreenshotDataRepository screenshotDataRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationProperties notificationProperties;

    @Override
    @Async
    public CompletableFuture<byte[]> generateReport(Long screenshotId) {
        try {
            // Fetch anomaly data from the database
            JsonNode anomalyDataJson = screenshotDataRepository.findAnomalyDataJsonByScreenshotId(screenshotId)
                    .orElseThrow(() -> {
                        log.warn("No anomaly data found for screenshot ID: {}", screenshotId);
                        return new RuntimeException("Anomaly data not found");
                    });

            // Deserialize JSON into AnomalyApiResponse
            AnomalyApiResponse anomalyData = objectMapper.treeToValue(anomalyDataJson, AnomalyApiResponse.class);

            // Create a PDF document
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                // Write anomaly data to the PDF
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.setLeading(14.5f);
                    contentStream.newLineAtOffset(50, 700);

                    contentStream.showText("Anomaly Report for Screenshot ID: " + screenshotId);
                    contentStream.newLine();
                    contentStream.newLine();
                    contentStream.showText("Is Anomaly: " + anomalyData.isAnomaly());
                    contentStream.newLine();
                    contentStream.showText("Anomaly Score: " + anomalyData.getAnomalyScore());
                    contentStream.newLine();
                    contentStream.showText("Explanations: " + String.join(", ", anomalyData.getExplanations()));
                    contentStream.newLine();
                    contentStream.showText("Resolutions: " + String.join(", ", anomalyData.getResolutions()));
                    contentStream.newLine();
                    contentStream.showText("Culprit Metrics: " + String.join(", ", anomalyData.getCulpritMetrics()));

                    contentStream.endText();
                }

                // Convert the PDF to a byte array
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    document.save(outputStream);
                    return CompletableFuture.completedFuture(outputStream.toByteArray());
                }
            }
        } catch (Exception e) {
            log.error("Error generating report for screenshot ID {}: {}", screenshotId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<String> prepareAndSendReportNotification(SendReportRequest request) {
        String recipientEmail = determineRecipientEmail(request);
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.warn("No valid recipient email provided for screenshotId: {}", request.getScreenshotId());
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Recipient email must be provided either directly or by selecting a predefined one.")
            );
        }

        return generateReport(request.getScreenshotId())
                .thenApply(pdfBytes -> {
                    String reportFileName = "anomaly_report_" + request.getScreenshotId() + ".pdf";
                    String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

                    ReportEventData reportEventData = ReportEventData.builder()
                            .recipientEmail(recipientEmail)
                            .reportContentBase64(pdfBase64)
                            .reportFileName(reportFileName)
                            .userName("Valued User")
                            .subject("Anomaly Report: " + reportFileName)
                            .build();

                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = objectMapper.convertValue(reportEventData, Map.class);
                    Event eventPayload = new Event(EventType.REPORT, dataMap);
                    eventPublisher.publishEvent(eventPayload);
                    log.info("Report event for screenshotId {} published for email {}", request.getScreenshotId(), recipientEmail);
                    return "Report for screenshotId " + request.getScreenshotId() + " is being processed to be sent to " + recipientEmail;
                });
    }

    @Override
    public Map<String, String> getPredefinedEmails() {
        return notificationProperties.getPredefinedEmails();
    }

    private String determineRecipientEmail(SendReportRequest request) {
        if (request.getCustomRecipientEmail() != null && !request.getCustomRecipientEmail().trim().isEmpty()) {
            return request.getCustomRecipientEmail();
        }
        if (request.getSelectedPredefinedEmailKey() != null &&
                notificationProperties.getPredefinedEmails().containsKey(request.getSelectedPredefinedEmailKey())) {
            return notificationProperties.getPredefinedEmails().get(request.getSelectedPredefinedEmailKey());
        }
        return null;
    }
}