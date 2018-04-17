CREATE OR REPLACE FUNCTION payment_method_mark_remove(
  _user_id          BIGINT,
  _payment_method_id BIGINT
)
  RETURNS payment_method AS $$
DECLARE
  _payment_method payment_method;
BEGIN
  UPDATE payment_method
  SET is_deleted = TRUE
  WHERE id = _payment_method_id AND user_id = _user_id
  RETURNING *
    INTO _payment_method;
  RETURN _payment_method;
END;
$$ LANGUAGE plpgsql;