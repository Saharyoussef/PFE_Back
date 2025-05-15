package com.sahar.dashboardservice.service;

import java.util.concurrent.CompletableFuture;

public interface ReportService {
    CompletableFuture<byte[]> generateReport(Long screenshotId);
}
