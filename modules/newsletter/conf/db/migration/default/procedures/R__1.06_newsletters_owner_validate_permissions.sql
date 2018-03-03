CREATE OR REPLACE FUNCTION newsletters_owner_validate_permissions(_user_id BIGINT, _newsletter_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  IF NOT exists(SELECT *
                FROM newsletters
                WHERE user_id = _user_id AND id = _newsletter_id)
  THEN
    RAISE '<ERROR>code=AUTHORIZATION,message=user does not have editor permissions<ERROR>';
  END IF;
END;
$$ LANGUAGE plpgsql STABLE;