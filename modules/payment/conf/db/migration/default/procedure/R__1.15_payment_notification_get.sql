CREATE OR REPLACE FUNCTION payment_notification_get(
  _key            TEXT,
  _user_id        BIGINT,
  _payment_system payment_system
)
  RETURNS JSONB AS $$
DECLARE
  _data JSONB;
BEGIN
  SELECT data INTO _data
  FROM payment_notification
  WHERE key = _key AND user_id = _user_id AND payment_system = _payment_system;

  RETURN _data;
END;
$$ LANGUAGE plpgsql STABLE;