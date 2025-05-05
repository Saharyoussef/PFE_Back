package com.sahar.dashboardservice.repository.implementation;

import com.sahar.dashboardservice.model.GrafanaDashboard;
import com.sahar.dashboardservice.repository.GrafanaDashboardRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import com.sahar.dashboardservice.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.function.BiFunction;

import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.*;
import static com.sahar.dashboardservice.utils.QueryUtils.*;
import static com.sahar.dashboardservice.utils.RequestUtils.randomUUID;
import static java.util.Map.of;

@Slf4j
@Repository
@RequiredArgsConstructor

public class GrafanaDashboardRepositoryImpl implements GrafanaDashboardRepository {
    private final JdbcClient jdbc;

    @Override
    public GrafanaDashboard createGrafanaDashboard(String name, String description, String url) {
        try {
            return jdbc.sql(CREATE_GRAFANA_FUNCTION).params(of("grafanadashboardUuid", randomUUID.get(), "name", name, "description", description, "url", url)).query(GrafanaDashboard.class).single();
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public GrafanaDashboard updateGrafanaDashboard(String grafanadashboardUuid, String name, String description,String url) {
        try {
            return jdbc.sql(UPDATE_DASHBOARD_FUNCTION).params(of( "grafanadashboardUuid", grafanadashboardUuid, "name", name, "description", description, "url", url)).query(GrafanaDashboard.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No grafana dashboardUuid found by UUID %s", grafanadashboardUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void deleteGrafanaDashboard(String grafanadashboardUuid) {
        try {
            jdbc.sql(DELETE_DASHBOARD_QUERY).params(of( "grafanadashboardUuid", grafanadashboardUuid)).update();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No grafana dashboardUuid found by UUID %s", grafanadashboardUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }

    }

    @Override
    public GrafanaDashboard getGrafanaDashboard(String grafanadashboardUuid) {
        try {
            return jdbc.sql(SELECT_DASHBOARD_QUERY).params(of("grafanadashboardUuid", grafanadashboardUuid)).query(GrafanaDashboard.class).single();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException(String.format("No grafana dashboard found user by UUID %s", grafanadashboardUuid));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

   /* @Override
    public List<GrafanaDashboard> getGrafanaDashboards(int page, int size, String filter) {
        try {
            var query = createSelectGrafanaDashboardsQuery(filter);
            return jdbc.sql(query).params(of("size", size, "filter", filter, "offset", offset.apply(size, page))).query(GrafanaDashboard.class).list();
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("No Grafana dashboard found");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }*/

    @Override
    public List<GrafanaDashboard> getGrafanaDashboards(int page, int size, String filter) {
        String query = null; // Declare outside try for logging in catch
        Map<String, Object> paramsMap = null; // Declare outside try for logging in catch
        try {
            query = createSelectGrafanaDashboardsQuery(filter); // Generate the query

            // Assuming offset.apply exists and calculates correctly (e.g., page * size for 0-based page)
            int calculatedOffset = offset.apply(size, page);

            // Prepare parameters map - FIX: Use "offset" key
            paramsMap = Map.of(
                    "size", size,
                    "filter", filter,
                    "offset", calculatedOffset // <-- Corrected parameter name key
            );

            // **Log SQL and Parameters before execution**
            log.debug("Executing getGrafanaDashboards SQL: {}", query);
            log.debug("SQL Parameters: {}", paramsMap);

            // Execute the query
            return jdbc.sql(query)
                    .params(paramsMap)
                    .query(GrafanaDashboard.class)
                    .list(); // .list() returns an empty list if no rows match, doesn't throw EmptyResultDataAccessException

        }
        // REMOVED catch (EmptyResultDataAccessException exception) - .list() handles this.
        catch (Exception exception) { // Catch generic Exception LAST
            // **Log the ORIGINAL exception stack trace**
            log.error("DATABASE ERROR executing getGrafanaDashboards. SQL: [{}], Params: [{}]", query, paramsMap, exception); // Log the original exception!

            // **Re-throw your custom exception AFTER logging the original**
            throw new ApiException("An error occurred while retrieving dashboards. Please check logs."); // Throw specific message
        }
    }

    @Override
    public int getPages(String userUuid, int page, int size, String filter) {
        try {
            var query = createSelectPagesQuery(filter);
            return jdbc.sql(query).params(of("size", size, "filter", filter)).query(Integer.class).single();
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private final BiFunction<Integer, Integer, Integer> offset = (size, page) -> page == 0 ? 0 : page == 1 ? size : size * page;
}
