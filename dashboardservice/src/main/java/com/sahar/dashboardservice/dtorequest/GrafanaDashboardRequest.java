package com.sahar.dashboardservice.dtorequest;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrafanaDashboardRequest {
    private String grafanadashboardId;
    private String grafanadashboardUuid;
    private String name;
    private String description;
    private String url;
    private String createdAt;
    private String updatedAt;
}
