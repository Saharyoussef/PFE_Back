package com.sahar.dashboardservice.dtoresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AnomalyApiResponse {
    @JsonProperty("is_anomaly")
    private boolean isAnomaly;

    @JsonProperty("anomaly_score")
    private double anomalyScore;

    @JsonProperty("explanations")
    private List<String> explanations;

    @JsonProperty("resolutions")
    private List<String> resolutions;

    @JsonProperty("culprit_metrics")
    private List<String> culpritMetrics;
}
