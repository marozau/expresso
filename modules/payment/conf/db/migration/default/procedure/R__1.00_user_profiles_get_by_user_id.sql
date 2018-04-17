CREATE OR REPLACE FUNCTION user_profiles_get_by_user_id(_user_id BIGINT)
  RETURNS user_profiles AS $$
DECLARE
  _user_profiles user_profiles;
BEGIN

  SELECT *
  INTO _user_profiles
  FROM user_profiles
  WHERE user_id = _user_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=USER_NOT_FOUND,message=user_id ''%'' not found<ERROR>', _user_id;
  END IF;

  RETURN _user_profiles;
END;
$$ LANGUAGE plpgsql STABLE;
