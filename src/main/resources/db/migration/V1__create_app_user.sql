CREATE TABLE app_user
(
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_id  varchar(255) NOT NULL UNIQUE,
    username     varchar(255) NOT NULL UNIQUE,
    display_name varchar(255) NOT NULL,
    created_at   timestamptz  NOT NULL
);
