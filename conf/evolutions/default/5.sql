# --- !Ups

CREATE TABLE mc_member (
  id                 TEXT PRIMARY KEY,
  email_address      TEXT,
  unique_email_id    TEXT,
  email_type         TEXT,
  status             TEXT,
  unsubscribe_reason TEXT,
  interests          TEXT,
  ip_signup          TEXT,
  timestamp_signup   TEXT,
  ip_opt             TEXT,
  timestamp_opt      TEXT,
  member_rating      INTEGER,
  language           TEXT,
  vip                BOOLEAN,
  email_client       TEXT,
  list_id            TEXT,
  last_changed       TEXT
);


# --- !Downs

DROP TABLE IF EXISTS mc_member CASCADE;