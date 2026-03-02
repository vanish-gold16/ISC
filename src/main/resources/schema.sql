ALTER TABLE IF EXISTS comments
    DROP CONSTRAINT IF EXISTS uk2ocgo3lfadb3wq0tx8wyt7sj2;

ALTER TABLE IF EXISTS notifications
    DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE IF EXISTS notifications
    ADD CONSTRAINT notifications_type_check CHECK (type IN (
        'LIKE',
        'FOLLOW',
        'FRIEND_REQUEST',
        'FRIEND_REQUEST_ACCEPT',
        'COMMENT',
        'MESSAGE',
        'SYSTEM'
    ));
