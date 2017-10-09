# --- !Ups

CREATE TABLE mc_activity_log (
  campaign_id   TEXT,
  email_address TEXT,
  action        TEXT,
  type          TEXT,
  timestamp     TEXT,
  url           TEXT,
  ip            TEXT,

  PRIMARY KEY (campaign_id, email_address, action, timestamp)
);


# --- !Downs

DROP TABLE IF EXISTS mc_activity_log CASCADE;