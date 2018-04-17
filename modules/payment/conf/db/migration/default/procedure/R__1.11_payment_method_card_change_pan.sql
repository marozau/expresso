-- it happens when encryption key expires or compromised
CREATE OR REPLACE FUNCTION payment_method_card_change_pan(
  _token TEXT,
  _pan   TEXT
)
  RETURNS BOOLEAN AS $$
DECLARE
BEGIN
  UPDATE card_pan_token
  SET pan = _pan
  WHERE token = _token;

  IF FOUND
  THEN
    RETURN TRUE;
  ELSE
    RETURN FALSE;
  END IF;
END;
$$ LANGUAGE plpgsql;