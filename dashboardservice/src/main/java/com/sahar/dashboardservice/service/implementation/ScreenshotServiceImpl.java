package com.sahar.dashboardservice.service.implementation;

import com.sahar.dashboardservice.dtoresponse.ScreenshotApiResponse;
import com.sahar.dashboardservice.exception.ApiException;
import com.sahar.dashboardservice.model.GrafanaDashboard;
import com.sahar.dashboardservice.model.Screenshot;
import com.sahar.dashboardservice.repository.GrafanaDashboardRepository;
import com.sahar.dashboardservice.repository.ScreenshotRepository;
import com.sahar.dashboardservice.service.ScreenshotDataService;
import com.sahar.dashboardservice.service.ScreenshotService;
import com.sahar.dashboardservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    private final ScreenshotRepository screenshotRepository;
    private final GrafanaDashboardRepository grafanaDashboardRepository;
    private final WebClient screenshotWebClient; // Ensure this bean is configured and injected
    private final UserService userService;
    private final ScreenshotDataService screenshotDataService; // Inject ScreenshotDataService

    @Override
    public Screenshot takeAndSaveScreenshot(String userUuid, String grafanadashboardUuid) {
        log.info("[User: {}] Requesting screenshot for dashboard UUID: {}", userUuid, grafanadashboardUuid);

        // 1. Get the full GrafanaDashboard object to extract its ID and URL
        GrafanaDashboard dashboard = grafanaDashboardRepository.findByUuid(grafanadashboardUuid)
                .orElseThrow(() -> {
                    log.warn("[User: {}] Dashboard not found with UUID: {}", userUuid, grafanadashboardUuid);
                    return new ApiException("Dashboard not found with UUID: " + grafanadashboardUuid);
                });

        // Extract the ID and URL from the full GrafanaDashboard object
        Long dashboardId = dashboard.getGrafanadashboardId();
        String grafanaUrl = dashboard.getUrl();
        log.debug("[User: {}] Found dashboard ID: {}, URL: {}", userUuid, dashboardId, grafanaUrl);

        // 2. Call Python API via WebClient
        log.debug("[User: {}] Calling Python screenshot API for URL: {}", userUuid, grafanaUrl);
        ScreenshotApiResponse apiResponse;
        try {
            apiResponse = screenshotWebClient.post()
                    .uri("/screenshot") // Your Python API endpoint
                    .body(Mono.just(Map.of("dashboard_url", grafanaUrl)), Map.class) // dashboard_url should be the same as in the fast api: class ScreenshotRequest(BaseModel): dashboard_url: HttpUrl
                    .retrieve()
                    .bodyToMono(ScreenshotApiResponse.class)
                    .block(); // Using .block() for simplicity; consider async for production

            if (apiResponse == null || apiResponse.getFilePath() == null || apiResponse.getFilePath().isBlank()) {
                log.error("[User: {}] Received invalid/empty response from screenshot API for URL: {}. Response: {}", userUuid, grafanaUrl, apiResponse);
                throw new ApiException("Failed to capture screenshot: Invalid response from Python API.");
            }
            log.info("[User: {}] Screenshot API successful. File path: {}", userUuid, apiResponse.getFilePath());

        } catch (WebClientResponseException e) {
            log.error("[User: {}] Error calling screenshot API for URL {}: Status={}, Body={}", userUuid, grafanaUrl, e.getStatusCode(), e.getResponseBodyAsString(), e);
            // You might want to throw a more specific exception or handle different statuses
            throw new ApiException("Failed to capture screenshot: Python API call failed with status " + e.getStatusCode());
        } catch (Exception e) {
            log.error("[User: {}] Unexpected error during screenshot API call for URL {}: {}", userUuid, grafanaUrl, e.getMessage(), e);
            throw new ApiException("Failed to capture screenshot: Unexpected error calling Python API.");
        }

        // 3. Save screenshot record via ScreenshotRepository
        String screenshotUuid = UUID.randomUUID().toString(); // Generate a new UUID for the screenshot
        String filePath = apiResponse.getFilePath();

        // Call the save method in your ScreenshotRepository
        Screenshot savedScreenshot = screenshotRepository.save(
                screenshotUuid,
                dashboardId,
                filePath
        );
        log.info("[User: {}] Saved screenshot record with UUID: {}", userUuid, savedScreenshot.getScreenshotUuid());

        // --- 4. Asynchronous Metrics Extraction and Anomaly Detection Chain ---
        // This part runs in the background. The takeAndSaveScreenshot method returns 'savedScreenshot' immediately.
        screenshotDataService.fetchAndSaveMetrics(savedScreenshot.getScreenshotId(), dashboard.getUrl())
                .thenComposeAsync(extractedMetricsList -> {
                    // This block executes after fetchAndSaveMetrics completes (successfully or with an empty list if error handled inside)
                    if (extractedMetricsList == null || extractedMetricsList.isEmpty()) {
                        log.warn("[ScreenshotID: {}] No metrics were extracted or an error occurred during extraction. Skipping anomaly detection.",
                                savedScreenshot.getScreenshotId());
                        return CompletableFuture.completedFuture(null);
                    }
                    log.info("[ScreenshotID: {}] Metrics successfully extracted ({} panels). Proceeding to anomaly detection.",
                            savedScreenshot.getScreenshotId(), extractedMetricsList.size());
                    return screenshotDataService.detectAndSaveAnomalies(savedScreenshot.getScreenshotId(), extractedMetricsList);
                })
                .whenComplete((anomalyResponse, ex) -> {
                    // 'ex' will be non-null if any unhandled exception occurred in the chain.

                    if (ex != null) {
                        log.error("[ScreenshotID: {}] Asynchronous metrics/anomaly processing failed: {}",
                                savedScreenshot.getScreenshotId(), ex.getMessage(), ex);
                        // At this point, metrics might be saved (or an empty record), but anomaly data wouldn't be.
                        // The user interface would eventually show that the anomaly detection part failed or is missing.
                    } else if (anomalyResponse != null) {
                        // Anomaly detection was attempted (or returned a default if no metrics for it inside detectAndSaveAnomalies)
                        boolean wasSkippedDueToNoMetrics = anomalyResponse.getExplanations() != null &&
                                anomalyResponse.getExplanations().contains("Anomaly detection skipped: No input metrics data.");

                        if (wasSkippedDueToNoMetrics) {
                            log.info("[ScreenshotID: {}] Anomaly detection was skipped by the service due to no input metrics.", savedScreenshot.getScreenshotId());
                            // No anomaly record was saved by the service in this specific "skipped" case.
                        } else {
                            log.info("[ScreenshotID: {}] Anomaly detection process completed. Is anomaly: {}. Score: {}",
                                    savedScreenshot.getScreenshotId(), anomalyResponse.isAnomaly(), anomalyResponse.getAnomalyScore());
                        }
                    } else {
                        log.info("[ScreenshotID: {}] Asynchronous processing chain completed; anomaly detection was skipped due to no metrics from the extraction phase.",
                                savedScreenshot.getScreenshotId());
                    }
                });

        // Return the initially saved screenshot object to the caller quickly.
        // The UI can indicate that metrics and anomaly analysis are "in progress".

        return savedScreenshot;
    }

    @Override
    @Async
    public void triggerScreenshotsForAllDashboards(String userUuid) {
        log.info("[User: {}] Triggering screenshots for ALL dashboards.", userUuid);
        List<String> allDashboardUuids = grafanaDashboardRepository.findAllDashboardUuids();

        if (allDashboardUuids.isEmpty()) {
            log.info("[User: {}] No dashboards found to screenshot.", userUuid);
            return;
        }

        log.info("[User: {}] Found {} dashboards. Starting screenshot process for each.", userUuid, allDashboardUuids.size());

        for (String dashboardUuid : allDashboardUuids) { // Iterate over UUIDs
            try {
                log.info("[User: {}] Taking screenshot for dashboard UUID: {}", userUuid, dashboardUuid);
                takeAndSaveScreenshot(userUuid, dashboardUuid); // Pass the UUID directly
                log.info("[User: {}] Successfully processed screenshot for dashboard UUID: {}", userUuid, dashboardUuid);
                // Optional: Thread.sleep(1000);
            } catch (ApiException e) {
                log.error("[User: {}] API Exception while taking screenshot for dashboard UUID {}: {}. Skipping.",
                        userUuid, dashboardUuid, e.getMessage());
            } catch (Exception e) {
                log.error("[User: {}] Unexpected error while taking screenshot for dashboard UUID {}. Skipping. Error: {}",
                        userUuid, dashboardUuid, e.getMessage(), e);
            }
        }
        log.info("[User: {}] Finished triggering screenshots for all dashboards.", userUuid);
    }

    @Override
    public void deleteScreenshot(String userUuid, String screenshotUuid) {
        userService.getUserByUuid(userUuid);
        screenshotRepository.deleteScreenshot(screenshotUuid);
    }

    @Override
    public Screenshot getScreenshot(String screenshotUuid) {
        return screenshotRepository.getScreenshot(screenshotUuid);
    }

    @Override
    public List<Screenshot> getScreenshots(String userUuid, int page, int size, String filterByDashboardName, String filterByDate) {
        // userService.getUserByUuid(userUuid); // This call is only for validation if user exists, not directly used in query
        log.info("[User: {}] Retrieving screenshots. Page: {}, Size: {}, DashboardNameFilter: {}, DateFilter: {}",
                userUuid, page, size, filterByDashboardName, filterByDate);
        return screenshotRepository.getScreenshots(page, size, filterByDashboardName, filterByDate);
    }


    @Override
    public int getPages(String userUuid, int page, int size, String filterByDashboardName, String filterByDate) {
        // var user = userService.getUserByUuid(userUuid);
        log.info("[User: {}] Calculating pages for screenshots. DashboardNameFilter: {}, DateFilter: {}",
                userUuid, filterByDashboardName, filterByDate);
        // Pass size and filters, not page (page is for fetching, not counting total pages)
        return screenshotRepository.getPages(size, filterByDashboardName, filterByDate);
    }
}

