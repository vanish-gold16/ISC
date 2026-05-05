package org.example.isc.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlEnvironmentPostProcessor";
    private static final String POSTGRESQL_SCHEME = "postgresql://";
    private static final String POSTGRES_SCHEME = "postgres://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String dbUrl = property(environment, "DB_URL");

        if (StringUtils.hasText(dbUrl) && !dbUrl.startsWith("jdbc:")) {
            addNormalizedPostgresUrl(environment, dbUrl);
            return;
        }

        String datasourceUrl = property(environment, "spring.datasource.url");
        if (missing(datasourceUrl)) {
            throw new IllegalStateException("""
                    Missing database configuration: set DB_URL in .env.properties, .env, or your run configuration.
                    Example: DB_URL=jdbc:postgresql://localhost:5432/isc_db
                    """);
        }

        if (!datasourceUrl.startsWith("jdbc:")) {
            throw new IllegalStateException("""
                    Invalid database configuration: DB_URL must be a JDBC URL starting with "jdbc:".
                    Example: DB_URL=jdbc:postgresql://localhost:5432/isc_db
                    """);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void addNormalizedPostgresUrl(ConfigurableEnvironment environment, String dbUrl) {
        if (!dbUrl.startsWith(POSTGRESQL_SCHEME) && !dbUrl.startsWith(POSTGRES_SCHEME)) {
            throw new IllegalStateException("""
                    Invalid database configuration: DB_URL must start with "jdbc:".
                    Plain PostgreSQL URLs are also accepted when they start with "postgresql://" or "postgres://".
                    """);
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", toJdbcPostgresUrl(dbUrl));
        addCredentialsFromUrl(environment, dbUrl, properties);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    private String toJdbcPostgresUrl(String dbUrl) {
        URI uri = URI.create(dbUrl);
        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            return dbUrl.startsWith(POSTGRES_SCHEME)
                    ? "jdbc:postgresql://" + dbUrl.substring(POSTGRES_SCHEME.length())
                    : "jdbc:" + dbUrl;
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(host);
        if (uri.getPort() != -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        if (StringUtils.hasText(uri.getRawPath())) {
            jdbcUrl.append(uri.getRawPath());
        }
        if (StringUtils.hasText(uri.getRawQuery())) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }
        return jdbcUrl.toString();
    }

    private void addCredentialsFromUrl(ConfigurableEnvironment environment, String dbUrl, Map<String, Object> properties) {
        String userInfo = URI.create(dbUrl).getRawUserInfo();
        if (!StringUtils.hasText(userInfo)) {
            return;
        }

        int separator = userInfo.indexOf(':');
        String username = separator >= 0 ? userInfo.substring(0, separator) : userInfo;
        String password = separator >= 0 ? userInfo.substring(separator + 1) : null;

        if (missing(property(environment, "DB_USERNAME")) && missing(property(environment, "spring.datasource.username"))) {
            properties.put("spring.datasource.username", decode(username));
        }
        if (StringUtils.hasText(password)
                && missing(property(environment, "DB_PASSWORD"))
                && missing(property(environment, "spring.datasource.password"))) {
            properties.put("spring.datasource.password", decode(password));
        }
    }

    private String property(ConfigurableEnvironment environment, String key) {
        try {
            return environment.getProperty(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean missing(String value) {
        return !StringUtils.hasText(value) || value.startsWith("${");
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
