CREATE OR REPLACE FUNCTION payment_method_card_delete_redundant()
  RETURNS BOOLEAN AS $$
DECLARE
  _payment_method RECORD;
BEGIN
  FOR _payment_method IN
  SELECT
    payment_method.id,
    payment_method.other ->> 'token' AS token
  FROM payment_method
  WHERE payment_method.payment_option = 'CARD'
        AND payment_method.expiration_date < timezone('UTC', now())
        OR (payment_method.is_deleted = TRUE AND payment_method.last_payment_timestamp ISNULL)
  LOOP
    DELETE FROM card_pan_token
    WHERE card_pan_token.token = _payment_method.token;

    /*DELETE FROM payment_method
    WHERE payment_method.id = _payment_method.id;*/
  END LOOP;
  RETURN TRUE;
END;
$$ LANGUAGE plpgsql;