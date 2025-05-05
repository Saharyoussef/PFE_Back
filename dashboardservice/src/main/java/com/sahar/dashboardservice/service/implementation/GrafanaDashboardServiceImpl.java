package com.sahar.dashboardservice.service.implementation;
import com.sahar.dashboardservice.model.GrafanaDashboard;
import com.sahar.dashboardservice.repository.GrafanaDashboardRepository;
import com.sahar.dashboardservice.service.GrafanaDashboardService;
import com.sahar.dashboardservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrafanaDashboardServiceImpl implements GrafanaDashboardService {
    private final GrafanaDashboardRepository grafanaDashboardRepository;
    private final UserService userService;
    @Override
    public GrafanaDashboard createGrafanaDashboard(String userUuid, String name, String description, String url) {
        userService.getUserByUuid(userUuid);
        return grafanaDashboardRepository.createGrafanaDashboard(name, description, url);
    }

    @Override
    public GrafanaDashboard updateGrafanaDashboard(String userUuid, String grafanadashboardUuid, String name, String description, String url) {
        userService.getUserByUuid(userUuid);
        return grafanaDashboardRepository.updateGrafanaDashboard(grafanadashboardUuid, name, description, url);
    }

    @Override
    public void deleteGrafanaDashboard(String userUuid, String grafanadashboardUuid) {
        userService.getUserByUuid(userUuid);
        grafanaDashboardRepository.deleteGrafanaDashboard(grafanadashboardUuid);
    }

    @Override
    public GrafanaDashboard getGrafanaDashboard(String grafanadashboardUuid) {
        return grafanaDashboardRepository.getGrafanaDashboard(grafanadashboardUuid);
    }

    @Override
    public List<GrafanaDashboard> getGrafanaDashboards(String userUuid,int page, int size, String filter) {
        userService.getUserByUuid(userUuid);
        return grafanaDashboardRepository.getGrafanaDashboards(page, size, filter);
    }

    @Override
    public int getPages(String userUuid, int page, int size, String filter) {
        var user = userService.getUserByUuid(userUuid);
        return grafanaDashboardRepository.getPages(userUuid, page, size, filter);
    }

}
