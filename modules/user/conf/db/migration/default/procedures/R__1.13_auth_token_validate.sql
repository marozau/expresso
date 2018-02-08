CREATE OR REPLACE FUNCTION auth_token_validate(_id UUID)
  RETURNS auth_token AS $$
DECLARE
  _auth_token auth_token;
BEGIN
  _auth_token = auth_token_find(_id);

  PERFORM auth_token_remove(_id);

  RETURN _auth_token;
END;
$$ LANGUAGE plpgsql;
