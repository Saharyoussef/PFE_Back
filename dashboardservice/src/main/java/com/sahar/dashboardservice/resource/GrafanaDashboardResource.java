package com.sahar.dashboardservice.resource;

import com.sahar.dashboardservice.domain.Response;
import com.sahar.dashboardservice.dtorequest.GrafanaDashboardRequest;
import com.sahar.dashboardservice.service.GrafanaDashboardService;
import com.sahar.dashboardservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import static com.sahar.dashboardservice.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@RequestMapping("/dashboard")
public class GrafanaDashboardResource {
    private final UserService userService;
    private final GrafanaDashboardService grafanaDashboardService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN')")
    public ResponseEntity<Response> createDashboard(@NotNull Authentication authentication,HttpServletRequest request, @RequestParam(value = "name", defaultValue = "") String name, @RequestParam(value = "description", defaultValue = "") String description, @RequestParam(value = "url", defaultValue = "") String url) {
        var dashboard = grafanaDashboardService.createGrafanaDashboard(authentication.getName(),name, description, url);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Starts from the base path of the current request (/dashboard/create)
                .path("/{id}")        // Appends the path variable template for the GET endpoint
                .buildAndExpand(dashboard.getGrafanadashboardUuid()) // Replaces {id} with the actual UUID
                .toUri();             // Creates the final URI (e.g., /dashboard/some-newly-generated-uuid)
        return ResponseEntity.created(location)
                .body(getResponse(request, of("dashboard", dashboard), "Dashboard created successfully", CREATED));

    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateDashboard(@NotNull Authentication authentication, HttpServletRequest request, @RequestBody GrafanaDashboardRequest dashboardRequest) {
        var dashboard = grafanaDashboardService.updateGrafanaDashboard(authentication.getName(), dashboardRequest.getGrafanadashboardUuid(), dashboardRequest.getName(), dashboardRequest.getDescription(), dashboardRequest.getUrl());
        return ok(getResponse(request, of("dashboard", dashboard), "dashboard updated", OK));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteDashboard(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "grafanadashboardUuid", defaultValue = "") String grafanadashboardUuid) {
        grafanaDashboardService.deleteGrafanaDashboard(authentication.getName(), grafanadashboardUuid);
        return ok(getResponse(request, emptyMap(), "Dashboard deleted", OK));
    }

    @GetMapping("/{grafanadashboardUuid}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Response> getDashboard(@NotNull Authentication authentication, HttpServletRequest request, @PathVariable(value = "grafanadashboardUuid") String grafanadashboardUuid) {
        var dashboard = grafanaDashboardService.getGrafanaDashboard(grafanadashboardUuid);
        return ok(getResponse(request, of("dashboard", dashboard), "Dashboard retrieved", OK));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Response> getDashboards(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "12") int size,@RequestParam(value = "filter", defaultValue = "") String filter) {
        var dashboards = grafanaDashboardService.getGrafanaDashboards(authentication.getName(), page, size, filter);
        var pages = grafanaDashboardService.getPages(authentication.getName(), page, size, filter);
        return ok(getResponse(request, of("dashboards", dashboards, "pages", pages), "Dashboards retrieved", OK));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') OR hasAuthority('ADMIN') OR hasAuthority('MANAGER')")
    public ResponseEntity<Response> getAllDashboards(@NotNull Authentication authentication, HttpServletRequest request, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "100") int size,@RequestParam(value = "filter", defaultValue = "") String filter) {
        var dashboards = grafanaDashboardService.getGrafanaDashboards(authentication.getName(), page, size, filter);
        return ok(getResponse(request, of("dashboards", dashboards), "Dashboards retrieved", OK));
    }
}
