CREATE OR REPLACE FUNCTION payment_method_update(
  _payment_method_id BIGINT,
  _status            payment_method_status,
  _details           JSONB,
  _display_name      TEXT,
  _expiration_date   TIMESTAMPTZ,
  _deleted           BOOLEAN
)
  RETURNS payment_method AS $$
DECLARE
  _payment_method payment_method;
BEGIN
  UPDATE payment_method
  SET
    status          = coalesce(_status, status),
    other           = CASE WHEN _details ISNULL
      THEN other
                      ELSE other || _details END,
    expiration_date = coalesce(_expiration_date, expiration_date),
    display_name    = coalesce(_display_name, display_name),
    is_deleted      = coalesce(_deleted, is_deleted)
  WHERE id = _payment_method_id
  RETURNING *
    INTO _payment_method;
  RETURN _payment_method;
END;
$$
LANGUAGE plpgsql;