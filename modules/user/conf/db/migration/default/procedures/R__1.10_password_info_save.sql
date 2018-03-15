CREATE OR REPLACE FUNCTION password_info_save(
  _provider_id  TEXT,
  _provider_key TEXT,
  _password     TEXT,
  _hasher       TEXT,
  _salt         TEXT)
  RETURNS password_info AS $$
DECLARE
  _password_info password_info;
BEGIN
  SELECT *
  INTO _password_info
  FROM password_info
  WHERE login_info_id = (SELECT id
                         FROM login_info
                         WHERE provider_id = _provider_id AND provider_key = _provider_key);

  IF NOT FOUND
  THEN
    RETURN password_info_add(_provider_id, _provider_key, _password, _hasher, _salt);
  ELSE
    RETURN password_info_update(_provider_id, _provider_key, _password, _hasher, _salt);
  END IF;
END;
$$ LANGUAGE plpgsql;
