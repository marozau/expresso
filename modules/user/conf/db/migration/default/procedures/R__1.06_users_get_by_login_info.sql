CREATE OR REPLACE FUNCTION users_get_by_login_info(_provider_id TEXT, _provider_key TEXT)
  RETURNS users AS $$
DECLARE
  _user users;
BEGIN
  SELECT *
  INTO _user
  FROM users
  WHERE id = (SELECT user_id
              FROM user_login_info
              WHERE login_info_id = (SELECT id
                                     FROM login_info
                                     WHERE provider_id = _provider_id AND provider_key = _provider_key));

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=USER_NOT_FOUND,message=user not found<ERROR>';
  END IF;

  RETURN _user;
END;
$$ LANGUAGE plpgsql STABLE;
