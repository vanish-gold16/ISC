CREATE TABLE IF NOT EXISTS schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_schedules_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
@@

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'subjects'
          AND column_name = 'name'
    ) THEN
        DROP TABLE IF EXISTS homeworks CASCADE;
        DROP TABLE IF EXISTS day_subject CASCADE;
        DROP TABLE IF EXISTS days CASCADE;
        DROP TABLE IF EXISTS schedules CASCADE;
        ALTER TABLE subjects DROP COLUMN name;
    END IF;
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.homeworks') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fkvb524a02ltj3yn9f0tgyfn38'
          AND conrelid = 'homeworks'::regclass
    ) THEN
        DROP TABLE homeworks CASCADE;
    END IF;
END;
$$;
@@

DO $$
DECLARE
    constraint_name TEXT;
    day_attnum INT;
BEGIN
    IF to_regclass('public.day_subject') IS NULL THEN
        RETURN;
    END IF;

    SELECT attnum INTO day_attnum
    FROM pg_attribute
    WHERE attrelid = 'day_subject'::regclass
      AND attname = 'day'
      AND NOT attisdropped;

    IF day_attnum IS NULL THEN
        RETURN;
    END IF;

    FOR constraint_name IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'day_subject'::regclass
          AND contype = 'u'
          AND array_length(conkey, 1) = 1
          AND conkey[1] = day_attnum
    LOOP
        EXECUTE format('ALTER TABLE day_subject DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END;
$$;
@@

DO $$
DECLARE
    constraint_name TEXT;
    sender_attnum INT;
BEGIN
    SELECT attnum INTO sender_attnum
    FROM pg_attribute
    WHERE attrelid = 'likes'::regclass
      AND attname = 'sender'
      AND NOT attisdropped;

    IF sender_attnum IS NOT NULL THEN
        SELECT conname INTO constraint_name
        FROM pg_constraint
        WHERE conrelid = 'likes'::regclass
          AND contype = 'u'
          AND array_length(conkey, 1) = 1
          AND conkey[1] = sender_attnum;

        IF constraint_name IS NOT NULL THEN
            EXECUTE format('ALTER TABLE likes DROP CONSTRAINT %I', constraint_name);
        END IF;
    END IF;
END;
$$;
@@

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_like_post_sender'
          AND conrelid = 'likes'::regclass
    ) THEN
        ALTER TABLE likes ADD CONSTRAINT uk_like_post_sender UNIQUE (post, sender);
    END IF;
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.profiles') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'profiles_occupation_check'
          AND conrelid = 'profiles'::regclass
    ) THEN
        ALTER TABLE profiles DROP CONSTRAINT profiles_occupation_check;
    END IF;

    ALTER TABLE profiles
        ADD CONSTRAINT profiles_occupation_check
            CHECK (
                occupation IS NULL OR occupation IN (
                    'IT',
                    'ENGINEERING',
                    'BUSINESS',
                    'FINANCE',
                    'LAW',
                    'MEDICINE',
                    'SCIENCE',
                    'EDUCATION',
                    'ART',
                    'SHOW_BUSINESS',
                    'DESIGN',
                    'MUSIC',
                    'MEDIA_AND_COMMUNICATION',
                    'HUMANITIES',
                    'SOCIAL_SCIENCES',
                    'SPORTS',
                    'TOURISM_AND_HOSPITALITY',
                    'AGRICULTURE_AND_ENVIRONMENT',
                    'TRANSPORT_AND_LOGISTICS',
                    'PUBLIC_SERVICE',
                    'MILITARY_AND_SECURITY',
                    'OTHER'
                )
            );
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.reviews') IS NULL THEN
        RETURN;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'reviews'
          AND column_name = 'user_id'
    ) THEN
        ALTER TABLE reviews ADD COLUMN user_id BIGINT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_reviews_user'
          AND conrelid = 'reviews'::regclass
    ) THEN
        ALTER TABLE reviews
            ADD CONSTRAINT fk_reviews_user
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'reviews_art_type_check'
          AND conrelid = 'reviews'::regclass
    ) THEN
        ALTER TABLE reviews DROP CONSTRAINT reviews_art_type_check;
    END IF;

    ALTER TABLE reviews
        ADD CONSTRAINT reviews_art_type_check
            CHECK (
                art_type IN (
                    'MUSIC',
                    'MOVIE',
                    'SHOW',
                    'GAME',
                    'ANIME',
                    'MANGA'
                )
            );
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.reviews') IS NULL OR to_regclass('public.review_criterion') IS NULL THEN
        RETURN;
    END IF;

    WITH ranked AS (
        SELECT
            rc.id,
            r.art_type,
            row_number() OVER (PARTITION BY rc.review ORDER BY rc.id) AS pos
        FROM review_criterion rc
        JOIN reviews r ON r.id = rc.review
        WHERE r.art_type IN ('ANIME', 'MANGA')
    )
    UPDATE review_criterion rc
    SET
        name = CASE ranked.pos
            WHEN 1 THEN 'Plot'
            WHEN 2 THEN 'Characters'
            WHEN 3 THEN 'World realism'
            WHEN 4 THEN 'Visual style'
            WHEN 5 THEN 'Ideologic'
            WHEN 6 THEN 'Emotional impact'
            WHEN 7 THEN 'Own experience'
            ELSE rc.name
        END,
        description = CASE ranked.pos
            WHEN 1 THEN 'not done yet'
            WHEN 2 THEN '(not done yet)'
            WHEN 3 THEN '(not done yet)'
            WHEN 4 THEN '(not done yet)'
            WHEN 5 THEN '(not done yet)'
            WHEN 6 THEN '(not done yet)'
            WHEN 7 THEN '(not done yet)'
            ELSE rc.description
        END,
        weight = CASE ranked.pos
            WHEN 1 THEN 20
            WHEN 2 THEN 15
            WHEN 3 THEN 10
            WHEN 4 THEN 10
            WHEN 5 THEN 10
            WHEN 6 THEN 10
            WHEN 7 THEN 25
            ELSE rc.weight
        END
    FROM ranked
    WHERE rc.id = ranked.id;

    UPDATE reviews r
    SET value = GREATEST(
        0,
        LEAST(
            100,
            CAST(
                ROUND(
                    COALESCE(
                        (
                            SELECT SUM(rc.value * rc.weight)
                            FROM review_criterion rc
                            WHERE rc.review = r.id
                        ),
                        0
                    ) / 10.0
                ) AS INTEGER
            )
        )
    )
    WHERE r.art_type IN ('ANIME', 'MANGA');
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.homeworks') IS NULL OR to_regclass('public.day_subject') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'homeworks'
          AND column_name = 'subject_id'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'homeworks'
          AND column_name = 'due_day_subject_id'
    ) THEN
        UPDATE homeworks hw
        SET subject_id = ds.subject
        FROM day_subject ds
        WHERE hw.subject_id IS NULL
          AND hw.due_day_subject_id IS NOT NULL
          AND ds.id = hw.due_day_subject_id;
    END IF;
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.homeworks') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'homeworks_priority_check'
          AND conrelid = 'homeworks'::regclass
    ) THEN
        ALTER TABLE homeworks DROP CONSTRAINT homeworks_priority_check;
    END IF;

    UPDATE homeworks
    SET priority = '_D97706'
    WHERE priority = '_FFFF00';

    ALTER TABLE homeworks
        ADD CONSTRAINT homeworks_priority_check
            CHECK (
                priority IS NULL OR priority IN (
                    '_00FF00',
                    '_D97706',
                    '_FFFF00',
                    '_FF0000',
                    '_6B21A8'
                )
            );
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.grades') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'grades'
          AND column_name = 'task'
    ) THEN
        ALTER TABLE grades DROP COLUMN task;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'grades'
          AND column_name = 'description'
    ) THEN
        ALTER TABLE grades ADD COLUMN description VARCHAR(2000);
    END IF;
END;
$$;
@@

DO $$
BEGIN
    IF to_regclass('public.homeworks') IS NULL THEN
        RETURN;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'homeworks'
          AND column_name = 'grade_id'
    ) THEN
        ALTER TABLE homeworks ADD COLUMN grade_id BIGINT;
    END IF;

    IF to_regclass('public.grades') IS NOT NULL AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_homeworks_grade'
          AND conrelid = 'homeworks'::regclass
    ) THEN
        ALTER TABLE homeworks
            ADD CONSTRAINT fk_homeworks_grade
                FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE SET NULL;
    END IF;
END;
$$;
@@

  CREATE TABLE IF NOT EXISTS user_settings (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL UNIQUE,
      settings_json TEXT NOT NULL,
      CONSTRAINT fk_user_settings_user
          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
  );
  @@
