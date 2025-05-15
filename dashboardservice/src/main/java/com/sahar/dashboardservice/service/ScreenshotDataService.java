package com.sahar.dashboardservice.service;


import com.sahar.dashboardservice.dtoresponse.AnomalyApiResponse;
import com.sahar.dashboardservice.dtoresponse.PanelData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScreenshotDataService {
    CompletableFuture<List<PanelData>> fetchAndSaveMetrics(Long screenshotId, String grafanaDashboardUrl);
    CompletableFuture<AnomalyApiResponse> detectAndSaveAnomalies(Long screenshotId, List<PanelData> metricsToAnalyze);
}
