# --- !Ups

CREATE TABLE recipients (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL REFERENCES users(id),
  list_name          TEXT        NOT NULL,
  user_ids           BIGINT []   NOT NULL DEFAULT ARRAY [] :: BIGINT [],
  is_default         BOOLEAN,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

DROP TRIGGER IF EXISTS trigger_recipients_modified
ON recipients;
CREATE TRIGGER trigger_recipients_modified
BEFORE UPDATE ON recipients
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_recipients_created
ON recipients;
CREATE TRIGGER trigger_recipients_created
BEFORE INSERT ON recipients
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


# --- !Downs

DROP TABLE IF EXISTS recipients CASCADE;
