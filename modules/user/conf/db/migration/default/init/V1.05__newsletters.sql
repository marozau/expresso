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