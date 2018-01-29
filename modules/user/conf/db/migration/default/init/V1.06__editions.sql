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

CREATE UNIQUE INDEX editions_newsletter_date_id_idx
  ON editions (newsletter_id, date);

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
