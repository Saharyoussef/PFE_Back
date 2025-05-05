package com.sahar.dashboardservice.service;

import com.sahar.dashboardservice.model.GrafanaDashboard;

import java.util.List;

public interface GrafanaDashboardService {
    GrafanaDashboard createGrafanaDashboard(String userUuid,String name, String description, String url);
    GrafanaDashboard updateGrafanaDashboard(String userUuid,String grafanadashboardUuid, String name, String description, String url);
    void deleteGrafanaDashboard(String userUuid,String grafanadashboardUuid);
    GrafanaDashboard getGrafanaDashboard(String grafanadashboardUuid);
    List<GrafanaDashboard> getGrafanaDashboards(String userUuid,int page, int size, String filter);
    int getPages(String userUuid, int page, int size, String filter);
}
