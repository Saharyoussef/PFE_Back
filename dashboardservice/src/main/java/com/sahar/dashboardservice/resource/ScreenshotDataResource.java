package com.sahar.dashboardservice.resource;

import com.sahar.dashboardservice.dtorequest.SendReportRequest;
import com.sahar.dashboardservice.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
@Slf4j
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

    @PostMapping("/send-report")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public CompletableFuture<ResponseEntity<String>> sendReportViaEmail(@Valid @RequestBody SendReportRequest request) {
        log.info("Received request to send report for screenshotId: {}", request.getScreenshotId());
        return reportService.prepareAndSendReportNotification(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("Error sending report for screenshotId {}: {}", request.getScreenshotId(), ex.getMessage(), ex.getCause());
                    String errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    if (ex.getCause() instanceof IllegalArgumentException) {
                        return ResponseEntity.badRequest().body(errorMessage);
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to process report sending: " + errorMessage);
                });
    }

    @GetMapping("/predefined-emails")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, String>> getPredefinedEmails() {
        return ResponseEntity.ok(reportService.getPredefinedEmails());
    }
}