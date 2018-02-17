CREATE OR REPLACE FUNCTION recipients_get_by_newsletter_id(_newsletter_id BIGINT, _user_id BIGINT, _status RECIPIENT_STATUS)
  RETURNS SETOF recipients AS $$
BEGIN
  IF _user_id NOTNULL
  THEN
    PERFORM newsletters_validate_permissions(_user_id, _newsletter_id);
  END IF;

  RETURN QUERY SELECT *
               FROM recipients
               WHERE newsletter_id = _newsletter_id AND (_status ISNULL OR status = _status);
END;
$$ LANGUAGE plpgsql STABLE;