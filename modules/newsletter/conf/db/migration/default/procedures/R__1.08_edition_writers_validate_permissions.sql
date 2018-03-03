CREATE OR REPLACE FUNCTION edition_writers_validate_permissions(_user_id BIGINT, _edition_id BIGINT)
  RETURNS VOID AS $$
DECLARE
  _newsletter_id BIGINT;
BEGIN
  IF exists(SELECT *
            FROM edition_writers
            WHERE user_id = _user_id AND edition_id = _edition_id)
  THEN
    RETURN;
  END IF;

  SELECT _newsletter_id
  INTO _newsletter_id
  FROM editions
  WHERE id = _edition_id;

  PERFORM newsletter_writers_validate_permissions(_user_id, _newsletter_id);
END;
$$ LANGUAGE plpgsql STABLE;