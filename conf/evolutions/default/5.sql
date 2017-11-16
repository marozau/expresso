# --- !Ups

CREATE TABLE newsletters (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL REFERENCES users (id),
  name               TEXT        NOT NULL,
  name_url           TEXT        NOT NULL,
  email              TEXT        NOT NULL,
  locale             TEXT        NOT NULL,
  logo_url           TEXT,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

CREATE UNIQUE INDEX newsletters_name_idx
  ON newsletters (name);

CREATE UNIQUE INDEX newsletters_name_url_idx
  ON newsletters (name_url);

DROP TRIGGER IF EXISTS trigger_newsletter_modified
ON newsletters;
CREATE TRIGGER trigger_newsletter_modified
BEFORE UPDATE ON newsletters
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_newsletter_created
ON newsletters;
CREATE TRIGGER trigger_newsletter_created
BEFORE INSERT ON newsletters
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


CREATE TABLE editions (
  id                 BIGSERIAL PRIMARY KEY,
  newsletter_id      BIGINT      NOT NULL REFERENCES newsletters (id),
  date               DATE        NOT NULL,
  url                TEXT,
  title              TEXT,
  header             TEXT,
  footer             TEXT,
  post_ids           BIGINT []   NOT NULL,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

CREATE INDEX editions_newsletter_id_idx
  ON editions (newsletter_id);

CREATE INDEX editions_modified_timestamp_idx
  ON editions (modified_timestamp);

DROP TRIGGER IF EXISTS trigger_editions_modified
ON editions;
CREATE TRIGGER trigger_editions_modified
BEFORE UPDATE ON editions
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_editions_created
ON editions;
CREATE TRIGGER trigger_editions_created
BEFORE INSERT ON editions
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


# --- !Downs
TRUNCATE posts CASCADE;
DROP TABLE IF EXISTS editions CASCADE;
DROP TABLE IF EXISTS newsletters CASCADE;