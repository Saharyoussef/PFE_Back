package com.sahar.dashboardservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Screenshot {
    private Long screenshotId;
    private String screenshotUuid;
    private Long grafanaDashboardId;
    private String url;
    private String createdAt;
    private String updatedAt;
}
