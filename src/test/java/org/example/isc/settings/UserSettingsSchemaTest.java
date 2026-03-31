package org.example.isc.settings;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UserSettingsSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void settingsJsonColumnUsesTextType() {
        String dataType = jdbcTemplate.queryForObject(
                """
                SELECT data_type
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'user_settings'
                  AND column_name = 'settings_json'
                """,
                String.class
        );

        assertEquals("text", dataType);
    }
}
