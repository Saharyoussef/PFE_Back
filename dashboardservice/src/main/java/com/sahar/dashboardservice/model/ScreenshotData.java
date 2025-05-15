package com.sahar.dashboardservice.model;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenshotData {
    private Long screenshotdataId;
    private String screenshotdataUuid;
    private Long screenshotId;
    private String metricsData; // JSONB stored as String
    private String anomalyData; // JSONB stored as String
    private String createdAt;
    private String updatedAt;
}
