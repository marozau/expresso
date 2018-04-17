CREATE OR REPLACE FUNCTION payment_method_card_get(_payment_method_id BIGINT)
  RETURNS TABLE(payment_method_id BIGINT, user_id BIGINT, expiration_date DATE, cardholder TEXT, token TEXT, pan TEXT) AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT
                 payment_method.id AS payment_method_id,
                 payment_method.user_id,
                 payment_method.expiration_date,
                 payment_method.other ->> 'cardholder' AS cardholder,
                 payment_method.other ->> 'token' AS token,
                 card_pan_token.pan
               FROM payment_method
                 INNER JOIN card_pan_token ON card_pan_token.token = payment_method.other ->> 'token'
               WHERE
                 payment_method.payment_option = 'CARD' AND
                 payment_method.id = _payment_method_id;
END;
$$ LANGUAGE plpgsql STABLE;