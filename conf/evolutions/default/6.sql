# --- !Ups

CREATE TABLE posts (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL REFERENCES users (id),
  newsletter_id      BIGINT REFERENCES newsletters (id),
  title              TEXT        NOT NULL,
  annotation         TEXT        NOT NULL,
  body               TEXT        NOT NULL,
  refs               TEXT []     NOT NULL,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

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

DROP TRIGGER IF EXISTS trigger_posts_created
ON posts;
CREATE TRIGGER trigger_posts_created
BEFORE INSERT ON posts
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


# --- !Downs

DROP TABLE IF EXISTS posts CASCADE;