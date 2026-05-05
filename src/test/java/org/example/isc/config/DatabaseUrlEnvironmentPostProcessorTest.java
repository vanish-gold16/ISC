package org.example.isc.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor postProcessor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    void acceptsJdbcDbUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DB_URL", "jdbc:postgresql://localhost:5432/isc_db")
                .withProperty("spring.datasource.url", "${DB_URL}");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://localhost:5432/isc_db");
    }

    @Test
    void normalizesPostgresqlUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DB_URL", "postgresql://localhost:5432/isc_db");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://localhost:5432/isc_db");
    }

    @Test
    void normalizesPostgresUrlAndExtractsCredentials() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DB_URL", "postgres://scott:tiger@localhost:5432/isc_db?sslmode=require");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://localhost:5432/isc_db?sslmode=require");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("scott");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("tiger");
    }

    @Test
    void rejectsMissingDbUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "${DB_URL}");

        assertThatThrownBy(() -> postProcessor.postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing database configuration");
    }

    @Test
    void rejectsUnsupportedUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DB_URL", "mysql://localhost:3306/isc_db");

        assertThatThrownBy(() -> postProcessor.postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must start with \"jdbc:\"");
    }
}
