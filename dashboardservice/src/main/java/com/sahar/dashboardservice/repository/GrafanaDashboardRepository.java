package com.sahar.dashboardservice.repository;

import com.sahar.dashboardservice.model.*;

import java.util.List;

public interface GrafanaDashboardRepository {
    GrafanaDashboard createGrafanaDashboard(String name, String description, String url);
    GrafanaDashboard updateGrafanaDashboard(String grafanadashboardUuid, String name, String description, String url);
    void deleteGrafanaDashboard(String grafanadashboardUuid);
    GrafanaDashboard getGrafanaDashboard(String grafanadashboardUuid);
    List<GrafanaDashboard> getGrafanaDashboards(int page, int size, String filter);
    int getPages(String userUuid, int page, int size, String filter);
}
