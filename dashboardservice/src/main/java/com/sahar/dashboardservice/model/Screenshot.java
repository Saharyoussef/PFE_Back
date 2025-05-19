package com.sahar.dashboardservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Screenshot {
    private Long screenshotId;
    private String screenshotUuid;
    private Long grafanadashboardId;
    private String url;
    private LocalDateTime createdAt;
    private String grafanaDashboardName; // To hold the joined dashboard name
}
