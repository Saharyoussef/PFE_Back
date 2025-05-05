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
public class GrafanaDashboard {
    private Long grafanadashboardId;
    private String grafanadashboardUuid;
    private String name;
    private String description;
    private String url;
    private String createdAt;
    private String updatedAt;
}
