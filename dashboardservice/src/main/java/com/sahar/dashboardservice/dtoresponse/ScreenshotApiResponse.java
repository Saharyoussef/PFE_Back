package com.sahar.dashboardservice.dtoresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScreenshotApiResponse {
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("message")
    private String message;
}
