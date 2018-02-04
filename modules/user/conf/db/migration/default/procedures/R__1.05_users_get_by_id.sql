CREATE OR REPLACE FUNCTION users_get_by_id(_user_id BIGINT)
  RETURNS users AS $$
DECLARE
  _user users;
BEGIN
  SELECT *
  INTO _user
  WHERE id = _user_id;

  IF NOT FOUND
    THEN
      RAISE '<ERROR>code=USER_NOT_FOUND,message=user_id ''%'' not found<ERROR>', _user_id;
  END IF;

  RETURN _user;
END;
$$ LANGUAGE plpgsql STABLE;
