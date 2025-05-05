package com.sahar.dashboardservice.utils;

import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.SELECT_GRAFANA_DASHBOARDS_QUERY;
import static com.sahar.dashboardservice.query.GrafanaDashboardQuery.SELECT_PAGE_QUERY;
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

    public static String createSelectPagesQuery( String filter) {
        var query = getStringBuilder(SELECT_PAGE_QUERY);
        if(isNotBlank(filter)) {
            query.append(" AND g.name ~* :filter");
        }
        return replace(query.toString(), "\\n", "");
    }

    private static StringBuilder getStringBuilder(String query) {
        return new StringBuilder(query);
    }




















}