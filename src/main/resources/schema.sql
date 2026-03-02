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
