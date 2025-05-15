package com.sahar.dashboardservice.resource;

import com.sahar.dashboardservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class ScreenshotDataResource {

    private final ReportService reportService;

    @GetMapping("/view-report")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public CompletableFuture<ResponseEntity<byte[]>> viewReport(@RequestParam Long screenshotId) {
        return reportService.generateReport(screenshotId)
                .thenApply(pdfReport -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=anomaly_report_" + screenshotId + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfReport))
                .exceptionally(ex -> {
                    return ResponseEntity.badRequest().body(null); // Return 400 if no report can be generated
                });
    }
}