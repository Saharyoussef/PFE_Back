package com.sahar.dashboardservice.service;

import com.sahar.dashboardservice.model.Screenshot;

import java.util.List;

public interface ScreenshotService {
    Screenshot takeAndSaveScreenshot(String userUuid, String grafanadashboardUuid);
    void triggerScreenshotsForAllDashboards(String userUuid);
    void deleteScreenshot(String userUuid, String screenshotUuid);
    Screenshot getScreenshot(String screenshotUuid);
    List<Screenshot> getScreenshots(String userUuid, int page, int size, String filterByDashboardName, String filterByDate);
    int getPages(String userUuid, int page, int size, String filterByDashboardName, String filterByDate);
}
