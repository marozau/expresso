CREATE OR REPLACE FUNCTION password_info_get(_provider_id TEXT, _provider_key TEXT)
  RETURNS SETOF password_info AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT *
               FROM password_info
               WHERE login_info_id = (SELECT id
                                      FROM login_info
                                      WHERE provider_id = _provider_id AND provider_key = _provider_key);
END;
$$ LANGUAGE plpgsql STABLE;
