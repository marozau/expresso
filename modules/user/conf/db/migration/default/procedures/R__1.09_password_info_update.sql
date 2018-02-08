CREATE OR REPLACE FUNCTION password_info_update(
  _provider_id  TEXT,
  _provider_key TEXT,
  _password     TEXT,
  _hasher       TEXT,
  _salt         TEXT)
  RETURNS password_info AS $$
DECLARE
  _login_info    login_info;
  _password_info password_info;
BEGIN
  SELECT *
  INTO _login_info
  FROM login_info
  WHERE provider_id = _provider_id AND provider_key = _provider_key;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=USER_NOT_FOUND,message=invalid login info<ERROR>';
  END IF;

  UPDATE password_info
  SET
    password = _password,
    hasher   = _hasher,
    salt     = _salt
  WHERE login_info_id = _login_info.id
  RETURNING *
    INTO _password_info;

  RETURN _password_info;
END;
$$ LANGUAGE plpgsql;