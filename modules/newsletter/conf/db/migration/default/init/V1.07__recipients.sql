CREATE TYPE RECIPIENT_STATUS AS ENUM ('PENDING', 'SUBSCRIBED', 'UNSUBSCRIBED', 'REMOVED', 'CLEANED', 'SPAM');

-- TODO: add locale for multilang newsletters
CREATE TABLE recipients (
  id                 UUID PRIMARY KEY          DEFAULT uuid_generate_v4(),
  user_id            BIGINT           NOT NULL,
  newsletter_id      BIGINT           NOT NULL REFERENCES newsletters (id),
  status             RECIPIENT_STATUS NOT NULL,
  created_timestamp  TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_timestamp TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP
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