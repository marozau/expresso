# --- !Ups

CREATE TABLE recipient_lists (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL REFERENCES users (id),
  name               TEXT        NOT NULL,
  is_default         BOOLEAN,
  created_timestamp  TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('UTC', now())
);

DROP TYPE IF EXISTS recipient_status CASCADE;
CREATE TYPE recipient_status AS ENUM ('SUBSCRIBED', 'UNSUBSCRIBED', 'REMOVED', 'CLEANED', 'SPAM');

CREATE TABLE recipients (
  list_id            BIGINT           NOT NULL REFERENCES lists (id),
  user_id            BIGINT           NOT NULL REFERENCES users (id),
  status             recipient_status NOT NULL DEFAULT 'SUBSCRIBED',
  created_timestamp  TIMESTAMPTZ      NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ      NOT NULL DEFAULT timezone('UTC', now())
);

CREATE UNIQUE INDEX recipient_lists_single_id_default_idx
  ON recipient_lists (user_id, is_default);

CREATE INDEX recipient_lists_id_and_user_id_idx
  ON recipient_lists (id, user_id);

CREATE UNIQUE INDEX recipient_lists_unique_name_idx
  ON recipient_lists (name);


CREATE INDEX recipient_lists_modified_timestamp_idx
  ON recipient_lists (modified_timestamp);

CREATE INDEX recipients_search_idx
  ON recipients (list_id, user_id, status);

CREATE INDEX recipients_modified_timestamp_idx
  ON recipients (modified_timestamp);

DROP TRIGGER IF EXISTS trigger_recipient_lists_modified
ON recipient_lists;
CREATE TRIGGER trigger_recipient_lists_modified
BEFORE UPDATE ON recipient_lists
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_recipient_lists_created
ON recipient_lists;
CREATE TRIGGER trigger_recipient_lists_created
BEFORE INSERT ON recipient_lists
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


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
DROP TABLE IF EXISTS recipient_lists CASCADE;
