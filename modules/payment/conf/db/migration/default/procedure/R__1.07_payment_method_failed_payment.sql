DROP FUNCTION IF EXISTS payment_method_failed_payment(BIGINT, TIMESTAMPTZ);

CREATE OR REPLACE FUNCTION payment_method_failed(
  _payment_method_id BIGINT,
  _date TIMESTAMPTZ
)
  RETURNS payment_method AS $$
DECLARE
  _payment_method payment_method;
BEGIN
  UPDATE payment_method SET last_failed_timestamp = _date WHERE id = _payment_method_id
  RETURNING *
    INTO _payment_method;
  RETURN _payment_method;
END;
$$ LANGUAGE plpgsql;