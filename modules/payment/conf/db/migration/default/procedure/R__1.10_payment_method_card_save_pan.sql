CREATE OR REPLACE FUNCTION payment_method_card_save_pan(
  _token             TEXT,
  _pan               TEXT
)
  RETURNS VOID AS $$
DECLARE
BEGIN
  INSERT INTO card_pan_token(token, pan) VALUES (_token, _pan);
END;
$$ LANGUAGE plpgsql;