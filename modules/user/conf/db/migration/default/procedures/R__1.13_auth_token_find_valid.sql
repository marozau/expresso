DROP FUNCTION IF EXISTS auth_token_find_valid(_id UUID);

CREATE OR REPLACE FUNCTION auth_token_find_valid(_id UUID)
  RETURNS auth_token AS $$
DECLARE
  _auth_token auth_token;
BEGIN
  SELECT *
  INTO _auth_token
  FROM auth_token
  WHERE id = _id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=INVALID_AUTH_TOKEN,message=auth_token not found<ERROR>';
  END IF;

  IF _auth_token.expiry < CURRENT_TIMESTAMP
  THEN
    RAISE '<ERROR>code=INVALID_AUTH_TOKEN,message=auth_token expired at ''%''<ERROR>', _auth_token.expiry;
  END IF;

  RETURN _auth_token;
END;
$$ LANGUAGE plpgsql STABLE;
