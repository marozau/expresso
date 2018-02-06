CREATE OR REPLACE FUNCTION password_info_remove(_provider_id TEXT, _provider_key TEXT)
  RETURNS VOID AS $$
DECLARE
BEGIN
  DELETE
  FROM password_info
  WHERE login_info_id = (SELECT id
                         FROM login_info
                         WHERE provider_id = _provider_id AND provider_key = _provider_key);
END;
$$ LANGUAGE plpgsql;
