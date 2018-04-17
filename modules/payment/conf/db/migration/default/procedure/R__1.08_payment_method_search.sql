CREATE OR REPLACE FUNCTION payment_method_search(
  _user_id BIGINT,
  _payment_system payment_system,
  _payment_option payment_option,
  _data JSONB,
  _successful_only BOOLEAN,
  _limit INT
)
  RETURNS SETOF payment_method AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT *
  FROM payment_method
  WHERE
    user_id = _user_id AND
    (_payment_option ISNULL OR payment_option = _payment_option) AND
    (_payment_system ISNULL OR payment_system = _payment_system) AND
    (_data ISNULL OR other @> _data) AND
    (NOT _successful_only OR last_payment_timestamp NOTNULL)
  LIMIT _limit;
END;
$$ STABLE LANGUAGE plpgsql STABLE;