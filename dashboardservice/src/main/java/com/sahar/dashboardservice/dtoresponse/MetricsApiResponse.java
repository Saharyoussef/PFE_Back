package com.sahar.dashboardservice.dtoresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class MetricsApiResponse { // This DTO is for the output of your /extract_metrics API
    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<PanelData> data; // The 'data' field is a List of PanelData objects
}
