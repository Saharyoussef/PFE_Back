package com.sahar.dashboardservice.service;

import com.sahar.dashboardservice.dtorequest.SendReportRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ReportService {
    CompletableFuture<byte[]> generateReport(Long screenshotId);
    CompletableFuture<String> prepareAndSendReportNotification(SendReportRequest request);
    Map<String, String> getPredefinedEmails();
}
