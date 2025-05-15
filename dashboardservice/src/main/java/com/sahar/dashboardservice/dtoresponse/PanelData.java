package com.sahar.dashboardservice.dtoresponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@NoArgsConstructor
public class PanelData {
    @JsonProperty("panel_title")
    private String panelTitle;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("values")
    private List<ValueEntry> values;


    @Data
    @NoArgsConstructor
    public static class ValueEntry {
        @JsonProperty("timestamp")
        private Long timestamp;

        @JsonProperty("value")
        private Double value;

        // If readable_date is per value point (as it seems in your example)
        @JsonProperty("readable_date")
        private String readableDate; // Anomaly detection API will ignore this if not in its Pydantic model
    }
}