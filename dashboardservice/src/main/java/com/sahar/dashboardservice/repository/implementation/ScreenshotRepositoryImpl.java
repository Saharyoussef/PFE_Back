package com.sahar.dashboardservice.repository.implementation;

import com.sahar.dashboardservice.exception.ApiException;
import com.sahar.dashboardservice.model.GrafanaDashboard;
import com.sahar.dashboardservice.model.Screenshot;
import com.sahar.dashboardservice.repository.ScreenshotRepository;
import com.sahar.dashboardservice.utils.QueryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;

import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.DELETE_DASHBOARD_QUERY;
import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.SELECT_DASHBOARD_QUERY;
import static com.sahar.dashboardservice.query.ScreenshotQuery.*;
import static com.sahar.dashboardservice.utils.QueryUtils.*;
import static java.util.Map.of;

@Repository
@RequiredArgsConstructor
@Slf4j

public class ScreenshotRepositoryImpl implements ScreenshotRepository {
    private final JdbcClient jdbc;

    @Override
    public Screenshot save(String screenshotUuid, Long grafanadashboardId, String url) {
        log.debug("Saving screenshot with UUID {}, dashboard ID {}, path {}", screenshotUuid, grafanadashboardId, url);
        Map<String, Object> paramsMap = of(
                "screenshotUuid", screenshotUuid,
                "grafanadashboardId", grafanadashboardId,
                "url", url
        );
        try {
            return jdbc.sql(CREATE_SCREENSHOT_FUNCTION)
                    .params(paramsMap)
                    .query(Screenshot.class) // Maps columns to Screenshot fields by name
                    .single();
        } catch (EmptyResultDataAccessException ex) {
            log.error("SQL function create_screenshot did not return a record after insert. Params: {}", paramsMap, ex);
            throw new ApiException("Failed to save or retrieve screenshot record after insert.");
        } catch (Exception exception) {
            log.error("Error saving screenshot record. Params: {}", paramsMap, exception);
            // Consider logging the actual SQL exception details from 'exception'
            throw new ApiException("An error occurred while saving the screenshot record.");
        }
    }

    @Override
    public void deleteScreenshot(String screenshotUuid) {
        try {
            jdbc.sql(DELETE_SCREENSHOT_QUERY).params(of( "screenshotUuid", screenshotUuid)).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No screenshot found by UUID %s", screenshotUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }

    }

    @Override
    public Screenshot getScreenshot(String screenshotUuid) {
        try {
            return jdbc.sql(SELECT_SCREENSHOT_QUERY).params(of("screenshotUuid", screenshotUuid)).query(Screenshot.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No screenshot found user by UUID %s", screenshotUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public List<Screenshot> getScreenshots(int page, int size, String filterByDashboardName, String filterByDate) {
        String query = null;
        Map<String, Object> paramsMap = new HashMap<>(); // Use HashMap to build params
        try {
            // Pass specific filters to the query builder
            query = QueryUtils.createSelectScreenshotsQuery(filterByDashboardName, filterByDate);
            int calculatedOffset = offset.apply(size, page);
            paramsMap.put("size", size);
            paramsMap.put("offset", calculatedOffset);
            if (StringUtils.isNotBlank(filterByDashboardName)) {
                paramsMap.put("dashboardNameFilter", filterByDashboardName);
            }
            if (StringUtils.isNotBlank(filterByDate)) {
                paramsMap.put("dateFilter", filterByDate);
            }
            log.debug("Executing getScreenshots SQL: {}", query);
            log.debug("SQL Parameters: {}", paramsMap);
            return jdbc.sql(query)
                    .params(paramsMap)
                    .query(Screenshot.class) // Assumes Screenshot model has grafanaDashboardName field if selected
                    .list();
        } catch (Exception exception) {
            log.error("DATABASE ERROR executing getScreenshots. SQL: [{}], Params: [{}]", query, paramsMap, exception);
            throw new ApiException("An error occurred while retrieving screenshots. Please check logs.");
        }
    }

    @Override
    public int getPages(int size, String filterByDashboardName, String filterByDate) {
        Map<String, Object> paramsMap = new HashMap<>();
        try {
            var query = QueryUtils.createSelectScreenshotsPagesQuery(filterByDashboardName, filterByDate);
            paramsMap.put("size", size);
            if (StringUtils.isNotBlank(filterByDashboardName)) {
                paramsMap.put("dashboardNameFilter", filterByDashboardName);
            }
            if (StringUtils.isNotBlank(filterByDate)) {
                paramsMap.put("dateFilter", filterByDate);
            }

            log.debug("Executing getPages SQL: {}", query);
            log.debug("SQL Parameters: {}", paramsMap);

            return jdbc.sql(query)
                    .params(paramsMap)
                    .query(Integer.class)
                    .single();
        } catch (Exception exception) {
            log.error("DATABASE ERROR executing getPages for screenshots. Params: {}", paramsMap, exception);
            throw new ApiException("An error occurred while calculating screenshot pages.");
        }
    }
    private final BiFunction<Integer, Integer, Integer> offset = (size, page) -> page == 0 ? 0 : page == 1 ? size : size * page;

}
