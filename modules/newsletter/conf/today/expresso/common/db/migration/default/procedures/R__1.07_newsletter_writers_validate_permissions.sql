CREATE OR REPLACE FUNCTION newsletter_writers_validate_permissions(_user_id BIGINT, _newsletter_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  IF exists(SELECT *
                FROM newsletters
                WHERE user_id = _user_id AND id = _newsletter_id)
  THEN
    RETURN;
  END IF;

  IF NOT exists(SELECT *
            FROM newsletter_writers
            WHERE user_id = _user_id AND newsletter_id = _newsletter_id)
  THEN
    RAISE '<ERROR>code=AUTHORIZATION,message=user is not owner or does not have writer permissions<ERROR>';
  END IF;

END;
$$ LANGUAGE plpgsql STABLE;