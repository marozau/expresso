# --- !Ups

DROP TYPE IF EXISTS campaign_status;
CREATE TYPE campaign_status AS ENUM ('NEW', 'PENDING', 'SENT');

CREATE TABLE campaigns (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT          NOT NULL REFERENCES users(id),
  newsletter_id      BIGINT          NOT NULL REFERENCES newsletters (id),
  name               TEXT            NOT NULL,
  subject            TEXT            NOT NULL,
  preview            TEXT,
  from_name          TEXT            NOT NULL,
  from_email         TEXT            NOT NULL,
  status             campaign_status NOT NULL,
  email_sent         INT             NOT NULL DEFAULT 0,
  send_time          TIMESTAMPTZ     NOT NULL,
  recipient_id       BIGINT          NOT NULL REFERENCES recipients (id),
  options            JSONB,
  created_timestamp  TIMESTAMPTZ     NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ     NOT NULL DEFAULT timezone('UTC', now())
);

CREATE INDEX campaigns_id_and_user_id_idx
  ON campaigns (id, user_id);

CREATE INDEX campaigns_modified_timestamp_idx
  ON campaigns (modified_timestamp);

DROP TRIGGER IF EXISTS trigger_campaigns_modified
ON campaigns;
CREATE TRIGGER trigger_campaigns_modified
BEFORE UPDATE ON campaigns
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_campaigns_created
ON campaigns;
CREATE TRIGGER trigger_campaigns_created
BEFORE INSERT ON campaigns
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();


# --- !Downs

DROP TABLE IF EXISTS campaigns CASCADE;
