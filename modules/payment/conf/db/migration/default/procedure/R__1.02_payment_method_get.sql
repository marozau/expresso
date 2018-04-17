CREATE OR REPLACE FUNCTION payment_method_get(
  _user_id                BIGINT,
  _payment_method_id      BIGINT DEFAULT NULL,
  _include_deleted        BOOLEAN DEFAULT FALSE,
  _include_not_successful BOOLEAN DEFAULT FALSE
)
  RETURNS SETOF payment_method AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT *
               FROM payment_method
               WHERE payment_method.id = CASE WHEN _payment_method_id ISNULL
                 THEN payment_method.id
                                            ELSE _payment_method_id END
                     AND payment_method.user_id = CASE WHEN _user_id ISNULL
                 THEN payment_method.user_id
                                                     ELSE _user_id END
                     AND payment_method.is_deleted = CASE WHEN _include_deleted
                 THEN payment_method.is_deleted
                                                        ELSE FALSE END
                     AND (_include_not_successful OR payment_method.last_payment_timestamp NOTNULL)
               ORDER BY payment_method.is_default DESC, payment_method.last_payment_timestamp DESC,
                 payment_method.id DESC;
END;
$$
LANGUAGE plpgsql STABLE;