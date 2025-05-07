package com.sahar.dashboardservice.repository;

import com.sahar.dashboardservice.model.Screenshot;

import java.util.List;

public interface ScreenshotRepository {
    Screenshot save(String screenshotUuid, Long grafanadashboardId, String url);
    void deleteScreenshot(String screenshotUuid);
    Screenshot getScreenshot(String screenshotUuid);
    List<Screenshot> getScreenshots(int page, int size, String filterByDashboardName, String filterByDate);
    int getPages(int size, String filterByDashboardName, String filterByDate);
}
