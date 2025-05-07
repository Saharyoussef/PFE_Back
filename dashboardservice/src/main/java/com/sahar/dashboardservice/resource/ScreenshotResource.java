package com.sahar.dashboardservice.resource;
import com.sahar.dashboardservice.domain.Response;
import com.sahar.dashboardservice.model.Screenshot;
import com.sahar.dashboardservice.service.ScreenshotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.sahar.dashboardservice.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class ScreenshotResource {

    private final ScreenshotService screenshotService;
    /**
     * Endpoint to trigger taking a screenshot for a specific Grafana dashboard and save its metadata.
     * The screenshot image itself is assumed to be handled by the Python service.
     * This endpoint only deals with the metadata (path, timestamp, etc.).
     */
    @PostMapping("/screenshots/{grafanadashboardUuid}")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Response> takeScreenshot(
            @NotNull Authentication authentication, // To get the user performing the action
            HttpServletRequest request,           // If needed by your getResponse helper
            @PathVariable("grafanadashboardUuid") String grafanadashboardUuid) {

        log.info("API Request: User {} attempting to take screenshot for dashboard UUID: {}",
                authentication.getName(), grafanadashboardUuid);

        Screenshot savedScreenshot = screenshotService.takeAndSaveScreenshot(
                authentication.getName(), // Pass the username/UUID
                grafanadashboardUuid);

        // Return a 200 OK with the details of the saved screenshot metadata
        // You could also return a 201 Created if you were to construct a URI to the new screenshot resource
        return ResponseEntity.ok(
                getResponse(request,
                        of("screenshot", savedScreenshot),
                        "Screenshot captured and metadata saved successfully.",
                        OK)
        );
    }

    @PostMapping("/screenshots/trigger-all")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Response> triggerAllScreenshots(@NotNull Authentication authentication, HttpServletRequest request) {
        String userUuid = authentication.getName();
        log.info("API Request: User {} triggering screenshots for all dashboards.", userUuid);
        screenshotService.triggerScreenshotsForAllDashboards(userUuid);

        // Because it's async, return 202 Accepted immediately
        return ResponseEntity.accepted() // HTTP 202 Accepted
                .body(getResponse(request,
                        null, // No specific data to return for an async trigger
                        "Process to take all screenshots has been initiated. This may take some time.",
                        HttpStatus.ACCEPTED));
    }

    @DeleteMapping("/screenshots/delete")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Response> deleteScreenshot(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "screenshotUuid", defaultValue = "") String screenshotUuid) {
        screenshotService.deleteScreenshot(authentication.getName(), screenshotUuid);
        return ok(getResponse(request, emptyMap(), "Screenshot deleted", OK));
    }

    @GetMapping("/screenshots/{screenshotUuid}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Response> getScreenshot(@NotNull Authentication authentication, HttpServletRequest request, @PathVariable(value = "screenshotUuid") String screenshotUuid) {
        var screenshot = screenshotService.getScreenshot(screenshotUuid);
        return ok(getResponse(request, of("screenshot", screenshot), "Screenshot retrieved", OK));
    }

    @GetMapping("/screenshots/list")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Response> getScreenshots(
            @NotNull Authentication authentication,
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "dashboardName", required = false) String filterByDashboardName, // New specific filter
            @RequestParam(value = "date", required = false) String filterByDate // New specific filter (e.g., "YYYY-MM-DD")
    ) {

        String userUuid = authentication.getName();
        log.info("API Request: User {} listing screenshots. Page: {}, Size: {}, DashboardName: {}, Date: {}",
                userUuid, page, size, filterByDashboardName, filterByDate);

        var screenshots = screenshotService.getScreenshots(userUuid, page, size, filterByDashboardName, filterByDate);
        var pages = screenshotService.getPages(userUuid, page, size, filterByDashboardName, filterByDate); // page here is for context if needed by getResponse, not for calculation
        return ok(getResponse(request, of("screenshots", screenshots, "pages", pages), "Screenshots retrieved", OK));
    }
}
