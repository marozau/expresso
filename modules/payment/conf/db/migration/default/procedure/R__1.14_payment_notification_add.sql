CREATE OR REPLACE FUNCTION payment_notification_add(
  _key            TEXT,
  _user_id        BIGINT,
  _payment_system payment_system,
  _data           JSONB
)
  RETURNS VOID AS $$
DECLARE
BEGIN
  INSERT INTO payment_notification(key, user_id, payment_system, data) VALUES (_key, _user_id, _payment_system, _data) ON CONFLICT (key) DO NOTHING ;
END;
$$ LANGUAGE plpgsql;