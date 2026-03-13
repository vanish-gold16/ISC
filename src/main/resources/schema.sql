CREATE TABLE IF NOT EXISTS schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_schedules_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
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
