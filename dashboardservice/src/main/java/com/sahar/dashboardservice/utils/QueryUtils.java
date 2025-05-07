package com.sahar.dashboardservice.utils;

import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.SELECT_GRAFANA_DASHBOARDS_QUERY;
import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.SELECT_PAGE_QUERY;
import static com.sahar.dashboardservice.query.ScreenshotQuery.SELECT_PAGE_SCREENSHOT_QUERY;
import static com.sahar.dashboardservice.query.ScreenshotQuery.SELECT_SCREENSHOTS_QUERY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.replace;

public class QueryUtils {

    public static String createSelectGrafanaDashboardsQuery(String filter) {
        var query = new StringBuilder(SELECT_GRAFANA_DASHBOARDS_QUERY); // Use StringBuilder
        boolean whereClauseAdded = false;

        if (isNotBlank(filter)) {
            query.append(" WHERE "); // Add WHERE keyword
            query.append("g.name ~* :filter");
            whereClauseAdded = true; // Mark that WHERE was added
        }

        query.append(" ORDER BY g.created_at DESC LIMIT :size OFFSET :offset");
        return replace(query.toString(), "\\n", ""); // Or just query.toString()
    }

    public static String createSelectScreenshotsQuery(String filterByDashboardName, String filterByDate) { // Add date filter
        var query = new StringBuilder(SELECT_SCREENSHOTS_QUERY); // SELECT_SCREENSHOTS_QUERY should include the JOIN
        boolean whereClauseAdded = false;

        if (isNotBlank(filterByDashboardName)) {
            query.append(" WHERE ");
            // Now 'gd.name' is valid because of the JOIN in the base query
            query.append("gd.name ~* :dashboardNameFilter"); // Use a specific parameter name
            whereClauseAdded = true;
        }

        if (isNotBlank(filterByDate)) { // Assuming filterByDate is a String like "YYYY-MM-DD"
            query.append(whereClauseAdded ? " AND " : " WHERE ");
            query.append("DATE(s.created_at) = TO_DATE(:dateFilter, 'YYYY-MM-DD')"); // Filter by date part
            whereClauseAdded = true;
        }
        // Add more date range filters if needed (e.g., dateFrom, dateTo)

        query.append(" ORDER BY s.created_at DESC LIMIT :size OFFSET :offset");
        return query.toString(); // Assuming replace was for newlines in multiline strings
    }

    public static String createSelectPagesQuery( String filter) {
        var query = getStringBuilder(SELECT_PAGE_QUERY);
        if(isNotBlank(filter)) {
            query.append(" AND g.name ~* :filter");
        }
        return replace(query.toString(), "\\n", "");
    }

    public static String createSelectScreenshotsPagesQuery(String filterByDashboardName, String filterByDate) { // Add date filter
        var query = new StringBuilder(SELECT_PAGE_SCREENSHOT_QUERY); // SELECT_PAGE_SCREENSHOT_QUERY should include JOIN
        boolean whereClauseAdded = true; // Base query has "WHERE 1=1"

        if (isNotBlank(filterByDashboardName)) {
            query.append(" AND gd.name ~* :dashboardNameFilter");
        }
        if (isNotBlank(filterByDate)) {
            query.append(" AND DATE(s.created_at) = TO_DATE(:dateFilter, 'YYYY-MM-DD')");
        }
        return query.toString();
    }

    private static StringBuilder getStringBuilder(String query) {
        return new StringBuilder(query);
    }




















}