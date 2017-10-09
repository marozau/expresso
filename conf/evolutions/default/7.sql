# --- !Ups

CREATE TABLE mc_campaign (
  id                 TEXT PRIMARY KEY,
  type               TEXT,
  create_time        TEXT,
  archive_url        TEXT,
  status             TEXT,
  emails_sent        INT,
  send_time          TEXT,
  content_type       TEXT,
  recipients_list_id TEXT,
  recipient_count    INT
);


# --- !Downs

DROP TABLE IF EXISTS mc_campaign CASCADE;