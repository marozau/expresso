CREATE OR REPLACE FUNCTION users_verify(_user_id BIGINT, _token UUID)
  RETURNS users AS $$
DECLARE
  _auth_token auth_token;
  _user       users;
BEGIN

  SELECT *
  INTO _auth_token
  FROM auth_token
  WHERE id = _token AND user_id = _user_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=INVALID_AUTH_TOKEN,message=auth_token not found<ERROR>';
  END IF;

  IF _auth_token.expiry < CURRENT_TIMESTAMP
  THEN
    RAISE '<ERROR>code=INVALID_AUTH_TOKEN,message=auth_token expired at ''%''<ERROR>', _auth_token.expiry;
  END IF;

  UPDATE users
  SET status = 'VERIFIED'
  WHERE id = _auth_token.user_id
  RETURNING *
    INTO _user;

  RETURN _user;

END;
$$ LANGUAGE plpgsql;
