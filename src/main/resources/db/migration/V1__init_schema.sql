-- Users
CREATE TABLE users
(
    id         UUID         NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Categories
CREATE TABLE categories
(
    id   UUID         NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_categories_name UNIQUE (name)
);

-- Tags
CREATE TABLE tags
(
    id   UUID         NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tags_name UNIQUE (name)
);

-- Posts
CREATE TABLE posts
(
    id           UUID         NOT NULL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    reading_time INTEGER      NOT NULL,
    status       VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    author_id    UUID         NOT NULL,
    category_id  UUID         NOT NULL,
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Post-Tags join table
CREATE TABLE post_tags
(
    post_id UUID NOT NULL,
    tag_id  UUID NOT NULL,
    CONSTRAINT pk_post_tags PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);
