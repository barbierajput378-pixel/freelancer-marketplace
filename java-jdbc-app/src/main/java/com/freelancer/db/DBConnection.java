package com.freelancer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralised JDBC connection factory.
 *
 * <p>All connection settings are read from environment variables so that no
 * credentials are ever hardcoded in source:
 * <ul>
 *   <li>{@code DB_HOST}     – Aiven service host</li>
 *   <li>{@code DB_PORT}     – Aiven service port</li>
 *   <li>{@code DB_USER}     – database user (e.g. avnadmin)</li>
 *   <li>{@code DB_PASSWORD} – database password</li>
 *   <li>{@code DB_NAME}     – database name (FreelancerMarketplace)</li>
 * </ul>
 *
 * <p>Aiven requires TLS, so the JDBC URL uses {@code sslMode=REQUIRED}. No CA
 * certificate is verified (acceptable for this assignment per the brief).
 */
public final class DBConnection {

    private DBConnection() {
        // utility class – no instances
    }

    /** Reads an environment variable, throwing a friendly error if it is missing. */
    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + key
                            + "\nSet DB_HOST, DB_PORT, DB_USER, DB_PASSWORD and DB_NAME before running.");
        }
        return value.trim();
    }

    /** Builds the JDBC URL from the host/port/name environment variables. */
    public static String buildUrl() {
        String host = requireEnv("DB_HOST");
        String port = requireEnv("DB_PORT");
        String name = requireEnv("DB_NAME");
        // Aiven mandates SSL; REQUIRED encrypts without verifying a CA certificate.
        return "jdbc:mysql://" + host + ":" + port + "/" + name + "?sslMode=REQUIRED";
    }

    /**
     * Opens a brand-new {@link Connection}. Callers should use try-with-resources
     * so the connection is always closed.
     */
    public static Connection getConnection() throws SQLException {
        String url = buildUrl();
        Properties props = new Properties();
        props.setProperty("user", requireEnv("DB_USER"));
        props.setProperty("password", requireEnv("DB_PASSWORD"));
        // Keep server round-trips predictable for a desktop app.
        props.setProperty("connectTimeout", "10000");
        return DriverManager.getConnection(url, props);
    }

    /**
     * Quick connectivity probe used at startup. Returns normally if a connection
     * can be opened; otherwise throws the underlying SQLException.
     */
    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Connection opened but is not valid.");
            }
        }
    }
}
