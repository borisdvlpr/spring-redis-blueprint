-- Users
CREATE TABLE IF NOT EXISTS users
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

-- Categories
CREATE TABLE IF NOT EXISTS categories
(
    id   UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Tags
CREATE TABLE IF NOT EXISTS tags
(
    id   UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Posts
CREATE TABLE IF NOT EXISTS posts
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    reading_time INTEGER      NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    author_id    UUID         NOT NULL REFERENCES users (id),
    category_id  UUID         NOT NULL REFERENCES categories (id)
);

-- Post-Tags join table
CREATE TABLE IF NOT EXISTS post_tags
(
    post_id UUID NOT NULL REFERENCES posts (id),
    tag_id  UUID NOT NULL REFERENCES tags (id),
    PRIMARY KEY (post_id, tag_id)
);
