CREATE OR REPLACE FUNCTION payment_method_add(
  _user_id                 BIGINT,
  _payment_option          payment_option,
  _payment_system          payment_system,
  _status                  payment_method_status,
  _expiration_date         TIMESTAMPTZ,
  _display_name            TEXT,
  _is_default              BOOLEAN,
  _first_payment_timestamp TIMESTAMPTZ,
  _last_payment_timestamp  TIMESTAMPTZ,
  _last_failed_timestamp   TIMESTAMPTZ,
  _other                   JSONB
)
  RETURNS payment_method AS $$
DECLARE
  _payment_method payment_method;
BEGIN
  IF _is_default
  THEN
    UPDATE payment_method
    SET is_default = FALSE
    WHERE user_id = _user_id;
  END IF;
  INSERT INTO payment_method (payment_option, payment_system, user_id, expiration_date, color, display_name, is_default, first_payment_timestamp, last_payment_timestamp, last_failed_timestamp, other, status)
  VALUES (_payment_option, _payment_system, _user_id, _expiration_date, _color, _display_name, _is_default, _first_payment_timestamp, _last_payment_timestamp, _last_failed_timestamp, _other, _status)
  RETURNING *
    INTO _payment_method;
  RETURN _payment_method;
END;
$$ LANGUAGE plpgsql;