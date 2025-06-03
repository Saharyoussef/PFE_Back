package com.sahar.dashboardservice.query;

public class GrafanaDashboardQuery {
    public static final String CREATE_GRAFANA_FUNCTION =
            """
            SELECT * FROM create_grafanadashboard(:grafanadashboardUuid, :name, :description, :url)
            """;
    public static final String DELETE_DASHBOARD_QUERY =
            """
            DELETE FROM grafanadashboard WHERE grafanadashboard_uuid = :grafanadashboardUuid
            """;

    public static final String UPDATE_DASHBOARD_FUNCTION = """
    SELECT
        o_grafanadashboard_id AS grafanadashboardId,
        o_grafanadashboard_uuid AS grafanadashboardUuid,
        o_name AS name,
        o_description AS description,
        o_url AS url,
        o_created_at AS createdAt,
        o_updated_at AS updatedAt
    FROM update_grafanadashboard(:grafanadashboardUuid, :name, :description, :url)
""";

    public static final String SELECT_GRAFANA_DASHBOARDS_QUERY =
            """
            SELECT
                g.grafanadashboard_id, g.grafanadashboard_uuid, g.name, g.description,
                g.url, g.created_at, g.updated_at
            FROM
                grafanadashboard g
            """;

    public static final String SELECT_DASHBOARD_QUERY =
            """
            SELECT
                g.grafanadashboard_id, g.grafanadashboard_uuid, g.name, g.description,
                g.url, g.created_at, g.updated_at
            FROM
                grafanadashboard g
            WHERE g.grafanadashboard_uuid = :grafanadashboardUuid
            """;

    public static final String SELECT_PAGE_QUERY =
            """
            SELECT (CEILING(COUNT(*) / :size ::NUMERIC(10, 5))) AS pages 
            FROM grafanadashboard g 
            WHERE 1 = 1
            """;

    public static final String SELECT_DASHBOARD_BY_UUID =
            """
            SELECT * FROM grafanadashboard WHERE grafanadashboard_uuid = :grafanadashboardUuid
            """;

    public static final String SELECT_ALL_DASHBOARD_UUIDS =
            """
            SELECT grafanadashboard_uuid
            FROM grafanadashboard
            ORDER BY created_at DESC
            """;
}
