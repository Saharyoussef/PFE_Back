package com.sahar.dashboardservice.query;

public class ScreenshotDataQuery {
    public static final String INSERT_METRICS_QUERY =
            """
            INSERT INTO screenshotdata (screenshotdata_uuid, screenshot_id, metrics_data)
            VALUES (:screenshotdataUuid, :screenshotId, :metricsData::jsonb)
            RETURNING *
            """;

    public static final String UPDATE_ANOMALY_QUERY =
            """
            UPDATE screenshotdata
            SET anomaly_data = :anomalyData::jsonb, updated_at = CURRENT_TIMESTAMP
            WHERE screenshot_id = :screenshotId
            RETURNING *
            """;

    public static final String SELECT_ANOMALY_DATA_BY_SCREENSHOT_ID =
            """
            SELECT anomaly_data FROM screenshotdata
            WHERE screenshot_id = :screenshotId
            """;
}
