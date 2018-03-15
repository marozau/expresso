CREATE TYPE CAMPAIGN_STATUS AS ENUM (
  'NEW',
  'PENDING',
  'SENDING',
  'SENT',
  'SUSPENDED_PENDING',
  'SUSPENDED_SENDING',
  'FORCED_SUSPENDED_NEW',
  'FORCED_SUSPENDED_PENDING',
  'FORCED_SUSPENDED_SENDING');

CREATE TABLE campaigns (
  user_id            BIGINT          NOT NULL,
  edition_id         BIGINT          NOT NULL REFERENCES editions (id) PRIMARY KEY,
  newsletter_id      BIGINT          NOT NULL REFERENCES newsletters (id),
  send_time          TIMESTAMPTZ     NOT NULL,
  status             CAMPAIGN_STATUS NOT NULL,
  preview            TEXT,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_timestamp TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX campaigns_user_id_status_idx
  ON campaigns (user_id, status);

CREATE INDEX campaigns_modified_timestamp_idx
  ON campaigns (modified_timestamp);

DROP TRIGGER IF EXISTS trigger_campaigns_modified
ON campaigns;
CREATE TRIGGER trigger_campaigns_modified
BEFORE UPDATE ON campaigns
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();