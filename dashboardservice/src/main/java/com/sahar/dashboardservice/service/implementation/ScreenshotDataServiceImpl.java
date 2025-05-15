package com.sahar.dashboardservice.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahar.dashboardservice.dtoresponse.AnomalyApiResponse;
import com.sahar.dashboardservice.dtoresponse.MetricsApiResponse;
import com.sahar.dashboardservice.dtoresponse.PanelData;
import com.sahar.dashboardservice.repository.ScreenshotDataRepository;
import com.sahar.dashboardservice.service.ScreenshotDataService;
import com.sahar.dashboardservice.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenshotDataServiceImpl implements ScreenshotDataService {
    private final ScreenshotDataRepository screenshotDataRepository;
    private final WebClient metricsWebClient; // Ensure this bean is configured
    private final WebClient anomalyWebClient; // Ensure this bean is configured
    private final ObjectMapper objectMapper; // For saving raw JSON to DB


    @Override
    @Async
    public CompletableFuture<List<PanelData>> fetchAndSaveMetrics(Long screenshotId, String grafanaDashboardUrl) {
        log.info("[ScreenshotID: {}] Fetching metrics for URL: {}", screenshotId, grafanaDashboardUrl);
        try {
            String grafanaDashboardUid = UrlUtils.extractUidFromUrl(grafanaDashboardUrl);
            log.info("[ScreenshotID: {}] Extracted UID: {}", screenshotId, grafanaDashboardUid);

            // Call Metrics API - it returns MetricsApiResponse
            MetricsApiResponse metricsWrapperResponse = metricsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/extract_metrics")
                            .queryParam("dashboard_uid", grafanaDashboardUid)
                            .build())
                    .retrieve()
                    .bodyToMono(MetricsApiResponse.class) // Deserialize into the wrapper
                    .block(); // Consider reactive chain

            if (metricsWrapperResponse == null || metricsWrapperResponse.getData() == null || metricsWrapperResponse.getData().isEmpty()) {
                log.warn("[ScreenshotID: {}] No metrics data found in API response for dashboard UID: {}", screenshotId, grafanaDashboardUid);
                screenshotDataRepository.saveMetricsData(screenshotId, "[]"); // Save empty JSON array for the data part
                return CompletableFuture.completedFuture(Collections.emptyList());
            }

            List<PanelData> extractedMetrics = metricsWrapperResponse.getData(); // Get the actual list
            log.info("[ScreenshotID: {}] Metrics API successful. Message: '{}'. {} panels extracted.",
                    screenshotId, metricsWrapperResponse.getMessage(), extractedMetrics.size());

            try {
                // Save the extracted List<PanelData> as JSON
                String rawMetricsJson = objectMapper.writeValueAsString(extractedMetrics);
                screenshotDataRepository.saveMetricsData(screenshotId, rawMetricsJson);
            } catch (JsonProcessingException e) {
                log.error("[ScreenshotID: {}] Error serializing extracted metrics data to JSON: {}", screenshotId, e.getMessage());
                // Continue, but metrics might not be saved correctly
            }
            return CompletableFuture.completedFuture(extractedMetrics);

        } catch (IllegalArgumentException e) {
            log.error("[ScreenshotID: {}] Error extracting UID from Grafana URL: {}", screenshotId, grafanaDashboardUrl, e);
            return CompletableFuture.failedFuture(e);
        } catch (WebClientResponseException e) {
            log.error("[ScreenshotID: {}] WebClient error during metrics extraction for {}: Status={}, Body={}",
                    screenshotId, grafanaDashboardUrl, e.getStatusCode(), e.getResponseBodyAsString(), e);
            screenshotDataRepository.saveMetricsData(screenshotId, "[]"); // Save empty on error
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("[ScreenshotID: {}] Unexpected error fetching metrics for screenshot ID {}: {}",
                    screenshotId, e.getMessage(), e);
            screenshotDataRepository.saveMetricsData(screenshotId, "[]"); // Save empty on error
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async
    public CompletableFuture<AnomalyApiResponse> detectAndSaveAnomalies(Long screenshotId, List<PanelData> metricsToAnalyze) {
        log.info("[ScreenshotID: {}] Detecting anomalies for {} panels.", screenshotId,
                (metricsToAnalyze != null ? metricsToAnalyze.size() : 0));

        AnomalyApiResponse defaultErrorResponse = new AnomalyApiResponse();
        defaultErrorResponse.setAnomaly(false); // Sensible default
        // defaultErrorResponse.setExplanations(List.of("Error during anomaly detection or no data to analyze.")); // Set only on actual error

        if (metricsToAnalyze == null || metricsToAnalyze.isEmpty()) {
            log.warn("[ScreenshotID: {}] No metrics data provided for anomaly detection. Skipping.", screenshotId);
            AnomalyApiResponse noMetricsForAnomaly = new AnomalyApiResponse();
            noMetricsForAnomaly.setAnomaly(false);
            noMetricsForAnomaly.setExplanations(List.of("Anomaly detection skipped: No input metrics data."));
            // Do NOT save this 'noMetricsForAnomaly' to the database. The absence of a record is the indicator.
            return CompletableFuture.completedFuture(noMetricsForAnomaly);
        }

        try {
            AnomalyApiResponse anomalyResponse = anomalyWebClient.post()
                    .uri("/anomaly-detection")
                    .bodyValue(metricsToAnalyze)
                    .retrieve()
                    .bodyToMono(AnomalyApiResponse.class)
                    .block(); // Consider reactive chain

            if (anomalyResponse == null) {
                log.warn("[ScreenshotID: {}] Anomaly Detection API returned null response.", screenshotId);
                defaultErrorResponse.setExplanations(List.of("Anomaly detection API returned a null response."));
                // Do NOT save this error response to DB if the policy is to have no record on error.
                return CompletableFuture.completedFuture(defaultErrorResponse); // Or failedFuture
            }

            log.info("[ScreenshotID: {}] Anomaly Detection API successful. Response: {}", screenshotId, anomalyResponse);
            try {
                screenshotDataRepository.saveAnomalyData(screenshotId, objectMapper.writeValueAsString(anomalyResponse));
            } catch (JsonProcessingException e) {
                log.error("[ScreenshotID: {}] Error serializing anomaly response to JSON: {}", screenshotId, e.getMessage());
                // Anomaly result might not be saved, but the detection happened.
            }
            return CompletableFuture.completedFuture(anomalyResponse);

        } catch (WebClientResponseException e) {
            log.error("[ScreenshotID: {}] WebClient error during anomaly detection: Status={}, Body={}",
                    screenshotId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("[ScreenshotID: {}] Unexpected error during anomaly detection for screenshot ID {}: {}",
                    screenshotId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}