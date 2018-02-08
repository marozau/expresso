CREATE OR REPLACE FUNCTION users_verify(_user_id BIGINT, _token UUID)
  RETURNS users AS $$
DECLARE
  _auth_token auth_token;
  _user       users;
BEGIN

  _auth_token = auth_token_validate(_token);

  IF _auth_token.user_id <> _user_id
  THEN
    RAISE '<ERROR>code=INVALID_AUTH_TOKEN,message=invalid user_id<ERROR>';
  END IF;

  UPDATE users
  SET status = 'VERIFIED'
  WHERE id = _auth_token.user_id
  RETURNING *
    INTO _user;

  RETURN _user;

END;
$$ LANGUAGE plpgsql;
