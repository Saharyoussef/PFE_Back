package com.sahar.dashboardservice.query;

public class ScreenshotQuery {
    public static final String CREATE_SCREENSHOT_FUNCTION =
            """
            SELECT
                f.o_screenshot_id        AS screenshot_id,
                f.o_screenshot_uuid      AS screenshot_uuid,
                f.o_grafanadashboard_id  AS grafanadashboard_id,
                f.o_url                  AS url,
                f.o_created_at           AS created_at
            FROM create_screenshot(:screenshotUuid, :grafanadashboardId, :url) f
            """;
    public static final String DELETE_SCREENSHOT_QUERY =
            """
            DELETE FROM screenshot WHERE screenshot_uuid = :screenshotUuid
            """;

    public static final String SELECT_SCREENSHOT_QUERY =
            """
            SELECT * FROM screenshot WHERE screenshot_uuid = :screenshotUuid
            """;

    public static final String SELECT_SCREENSHOTS_QUERY =
            """
            SELECT
                s.screenshot_id, s.screenshot_uuid, s.grafanadashboard_id, s.url, s.created_at,
                gd.name AS grafanaDashboardName -- Optionally include dashboard name in results
            FROM
                screenshot s
            JOIN
                grafanadashboard gd ON s.grafanadashboard_id = gd.grafanadashboard_id
            """;
    public static final String SELECT_PAGE_SCREENSHOT_QUERY =
            """
            SELECT (CEILING(COUNT(s.*) / :size ::NUMERIC(10, 5))) AS pages
            FROM screenshot s
            JOIN grafanadashboard gd ON s.grafanadashboard_id = gd.grafanadashboard_id
            WHERE 1 = 1
            """;

}
