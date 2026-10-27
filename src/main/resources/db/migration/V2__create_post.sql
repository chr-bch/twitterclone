CREATE TABLE post
(
    id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id  bigint      NOT NULL REFERENCES app_user (id),
    content    text        NOT NULL
        CONSTRAINT post_content_length CHECK (char_length(content) BETWEEN 1 AND 280),
    created_at timestamptz NOT NULL
);

CREATE INDEX post_feed_idx ON post (created_at DESC, id DESC);
CREATE INDEX post_author_feed_idx ON post (author_id, created_at DESC, id DESC);
