CREATE TABLE newsletters (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL,
  name               TEXT        NOT NULL,
  locale             TEXT        NOT NULL,
  logo_url           TEXT,
  avatar_url         TEXT,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX newsletters_name_idx
  ON newsletters (name);

DROP TRIGGER IF EXISTS trigger_newsletter_modified
ON newsletters;
CREATE TRIGGER trigger_newsletter_modified
BEFORE UPDATE ON newsletters
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();