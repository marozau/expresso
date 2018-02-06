CREATE OR REPLACE FUNCTION auth_token_find_valid(_id UUID)
  RETURNS SETOF auth_token AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT *
               FROM auth_token
               WHERE id = _id AND expiry > CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql STABLE;
