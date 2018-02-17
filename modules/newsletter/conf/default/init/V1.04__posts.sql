CREATE TABLE posts (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL,
  edition_id         BIGINT      NOT NULL REFERENCES editions (id),
  edition_order      INT         NOT NULL,
  title              TEXT        NOT NULL,
  annotation         JSONB       NOT NULL,
  body               JSONB       NOT NULL,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX posts_title_url_idx
  ON posts (edition_id, title_url);

CREATE UNIQUE INDEX posts_edition_order_unique_idx
  ON posts (edition_id, edition_order);

CREATE UNIQUE INDEX posts_title_unique_idx
  ON posts (edition_id, title);

CREATE INDEX posts_id_and_user_id_idx
  ON posts (id, user_id);

CREATE INDEX posts_modified_timestamp_idx
  ON posts (modified_timestamp);

DROP TRIGGER IF EXISTS trigger_posts_modified
ON posts;
CREATE TRIGGER trigger_posts_modified
BEFORE UPDATE ON posts
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();
