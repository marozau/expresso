# --- !Ups

DROP TYPE IF EXISTS campaign_status;
CREATE TYPE campaign_status AS ENUM ('NEW', 'PENDING', 'SENDING', 'SENT');

CREATE TABLE campaigns (
  id                 BIGSERIAL PRIMARY KEY,
  edition_id         BIGINT          NOT NULL REFERENCES editions (id),
  preview            TEXT,
  status             campaign_status NOT NULL,
  send_time          TIMESTAMPTZ     NOT NULL,
  options            JSONB,
  created_timestamp  TIMESTAMPTZ     NOT NULL DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ     NOT NULL DEFAULT timezone('UTC', now())
);

CREATE UNIQUE INDEX campaigns_edition_id_unique_idx
  ON campaigns (edition_id);

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
