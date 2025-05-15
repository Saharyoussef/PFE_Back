package com.sahar.dashboardservice.repository.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahar.dashboardservice.model.GrafanaDashboard;
import com.sahar.dashboardservice.model.ScreenshotData;
import com.sahar.dashboardservice.repository.ScreenshotDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.SELECT_DASHBOARD_BY_UUID;
import static com.sahar.dashboardservice.query.ScreenshotDataQuery.*;
import static com.sahar.dashboardservice.utils.RequestUtils.randomUUID;
import static java.util.Map.of;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScreenshotDataRepositoryImpl implements ScreenshotDataRepository {
    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public ScreenshotData saveMetricsData(Long screenshotId, String metricsData) {
        try {
            return jdbc.sql(INSERT_METRICS_QUERY)
                    .params(of(
                            "screenshotdataUuid", randomUUID.get(),
                            "screenshotId", screenshotId,
                            "metricsData", metricsData
                    ))
                    .query(ScreenshotData.class)
                    .single();
        } catch (Exception exception) {
            log.error("Error saving metrics data for screenshot ID {}: {}", screenshotId, exception.getMessage());
            throw new RuntimeException("Failed to save metrics data", exception);
        }
    }

    @Override
    public ScreenshotData saveAnomalyData(Long screenshotId, String anomalyData) {
        try {
            return jdbc.sql(UPDATE_ANOMALY_QUERY)
                    .params(of(
                            "screenshotId", screenshotId,
                            "anomalyData", anomalyData
                    ))
                    .query(ScreenshotData.class)
                    .single();
        } catch (Exception exception) {
            log.error("Error saving anomaly data for screenshot ID {}: {}", screenshotId, exception.getMessage());
            throw new RuntimeException("Failed to save anomaly data", exception);
        }
    }

    @Override
    public Optional<JsonNode> findAnomalyDataJsonByScreenshotId(Long screenshotId) {
        log.debug("Fetching anomaly_data for screenshot_id: {}", screenshotId);
        try {
                 String anomalyDataString = jdbc.sql(SELECT_ANOMALY_DATA_BY_SCREENSHOT_ID)
                         .param("screenshotId", screenshotId) // Ensure param name matches placeholder if not positional
                         .query(String.class) // Expecting a single String result
                         .optional().orElse(null); // Get the string or null if not found

            if (anomalyDataString == null || anomalyDataString.trim().isEmpty() || "null".equalsIgnoreCase(anomalyDataString.trim())) {
                log.debug("No anomaly_data string found or it's null/empty for screenshot_id: {}", screenshotId);
                return Optional.empty();
            }

            // Parse the string into JsonNode
            JsonNode anomalyJsonNode = objectMapper.readTree(anomalyDataString);
            return Optional.of(anomalyJsonNode); // Return Optional of JsonNode

        } catch (EmptyResultDataAccessException ex) {
            log.debug("No screenshotdata record found for screenshot_id: {}", screenshotId);
            return Optional.empty(); // No row found, which is a valid case
        } catch (JsonProcessingException e) {
            log.error("Failed to parse anomaly_data JSON for screenshot_id {}: {}", screenshotId, e.getMessage());
            return Optional.empty();
        } catch (Exception exception) {
            log.error("Error fetching anomaly_data for screenshot_id {}: {}", screenshotId, exception.getMessage(), exception);
            throw new RuntimeException("Database error fetching anomaly data for screenshot_id: " + screenshotId, exception);
        }
    }

}
