CREATE OR REPLACE FUNCTION auth_token_save(_user_id BIGINT, _expiry TIMESTAMPTZ)
  RETURNS auth_token AS $$
DECLARE
  _token      UUID = uuid_generate_v4();
  _auth_token auth_token;
BEGIN
  INSERT INTO auth_token (id, user_id, expiry) VALUES (_token, _user_id, _expiry)
  RETURNING *
    INTO _auth_token;

  RETURN _auth_token;
END;
$$ LANGUAGE plpgsql;
