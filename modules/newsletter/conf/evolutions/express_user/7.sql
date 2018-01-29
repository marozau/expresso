# --- !Ups

CREATE TYPE recipient_status AS ENUM ('PENDING', 'SUBSCRIBED', 'UNSUBSCRIBED', 'REMOVED', 'CLEANED', 'SPAM');

CREATE TABLE recipients (
  id                 UUID PRIMARY KEY          DEFAULT uuid_generate_v4(),
  newsletter_id      BIGINT           NOT NULL REFERENCES newsletters (id),
  user_id            BIGINT           NOT NULL REFERENCES users (id),
  status             recipient_status NOT NULL,
  created_timestamp  TIMESTAMPTZ      NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ      NOT NULL DEFAULT timezone('UTC', now())
);

CREATE UNIQUE INDEX recipients_unique_subscription_idx
  ON recipients (newsletter_id, user_id);

CREATE INDEX recipients_search_idx
  ON recipients (newsletter_id);

CREATE INDEX recipients_subscribed_search_idx
  ON recipients (newsletter_id, user_id, status);

CREATE INDEX recipients_modified_timestamp_idx
  ON recipients (modified_timestamp);

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
DROP TYPE IF EXISTS recipient_status CASCADE;
