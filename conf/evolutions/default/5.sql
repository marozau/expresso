# --- !Ups

CREATE TABLE newsletters (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT REFERENCES users (id),
  name               TEXT        NOT NULL,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

CREATE UNIQUE INDEX newsletters_name_idx
  ON newsletters (name);

CREATE TABLE newsletters (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL REFERENCES users (id),
  url                TEXT,
  title              TEXT,
  header             TEXT,
  footer             TEXT,
  post_ids           BIGINT []   NOT NULL,
  options            JSONB,
  publish_timestamp  TIMESTAMPTZ,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

CREATE INDEX newsletters_id_and_user_id_idx
  ON newsletters (id, user_id);

CREATE INDEX newsletters_modified_timestamp_idx
  ON newsletters (modified_timestamp);

DROP TRIGGER IF EXISTS trigger_newsletters_modified
ON newsletters;
CREATE TRIGGER trigger_newsletters_modified
BEFORE UPDATE ON newsletters
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_newsletters_created
ON newsletters;
CREATE TRIGGER trigger_newsletters_created
BEFORE INSERT ON newsletters
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


# --- !Downs

DROP TABLE IF EXISTS newsletters CASCADE;