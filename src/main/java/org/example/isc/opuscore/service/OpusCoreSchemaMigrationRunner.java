package org.example.isc.opuscore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OpusCoreSchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OpusCoreSchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public OpusCoreSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        apply("ALTER TABLE IF EXISTS art_requests ALTER COLUMN description TYPE VARCHAR(1000)");
        apply("ALTER TABLE IF EXISTS art_requests ALTER COLUMN cover_url TYPE VARCHAR(1000)");
        apply("ALTER TABLE IF EXISTS art_requests ALTER COLUMN admin_note TYPE VARCHAR(1000)");
        apply("ALTER TABLE IF EXISTS art_requests ALTER COLUMN rejection_reason TYPE VARCHAR(1000)");

        apply("ALTER TABLE IF EXISTS artworks ALTER COLUMN description TYPE VARCHAR(1000)");
        apply("ALTER TABLE IF EXISTS artworks ALTER COLUMN cover_url TYPE VARCHAR(1000)");

        apply("ALTER TABLE IF EXISTS reviews ALTER COLUMN photo_url TYPE VARCHAR(1000)");
        apply("""
                DO $$
                BEGIN
                    IF to_regclass('public.notifications') IS NULL THEN
                        RETURN;
                    END IF;

                    IF EXISTS (
                        SELECT 1
                        FROM pg_constraint
                        WHERE conname = 'notifications_type_check'
                          AND conrelid = 'notifications'::regclass
                    ) THEN
                        ALTER TABLE notifications DROP CONSTRAINT notifications_type_check;
                    END IF;

                    ALTER TABLE notifications
                        ADD CONSTRAINT notifications_type_check
                            CHECK (
                                type IN (
                                    'LIKE',
                                    'FOLLOW',
                                    'FRIEND_REQUEST',
                                    'FRIEND_REQUEST_ACCEPT',
                                    'COMMENT',
                                    'MESSAGE',
                                    'SYSTEM',
                                    'REVIEW_STATUS',
                                    'ART_REQUEST_STATUS'
                                )
                            );
                END;
                $$;
                """);
    }

    private void apply(String sql) {
        jdbcTemplate.execute(sql);
        log.info("Applied OpusCore schema migration: {}", sql);
    }
}
