CREATE TABLE payment_notification (
  key               TEXT PRIMARY KEY,
  user_id           BIGINT,
  payment_system    payment_system,
  data              JSONB,
  created_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);