package com.sahar.dashboardservice.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.sahar.dashboardservice.model.ScreenshotData;

import java.util.Optional;

public interface ScreenshotDataRepository {
    ScreenshotData saveMetricsData(Long screenshotId, String metricsData);
    ScreenshotData saveAnomalyData(Long screenshotId, String anomalyData);
    Optional<JsonNode> findAnomalyDataJsonByScreenshotId(Long screenshotId);
}
