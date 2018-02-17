CREATE OR REPLACE FUNCTION recipients_add(_user_id BIGINT, _newsletter_id BIGINT, _status RECIPIENT_STATUS)
  RETURNS recipients AS $$
DECLARE
  _recipient recipients;
BEGIN
  INSERT INTO expresso.public.recipients (user_id, newsletter_id, status) VALUES (_user_id, _newsletter_id, coalesce(_status, 'PENDING'))
  RETURNING *
    INTO _recipient;

  RETURN _recipient;

  EXCEPTION WHEN unique_violation
  THEN
    SELECT *
    INTO _recipient
    FROM recipients
    WHERE user_id = _user_id AND newsletter_id = _newsletter_id
    FOR UPDATE;

    IF _recipient.status <> 'SUBSCRIBED'
    THEN
      UPDATE recipients
      SET status = 'PENDING'
      WHERE user_id = _user_id AND newsletter_id = _newsletter_id
      RETURNING *
        INTO _recipient;
    END IF;

    RETURN _recipient;
END;
$$ LANGUAGE plpgsql;