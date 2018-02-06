CREATE OR REPLACE FUNCTION auth_token_find(_id UUID)
  RETURNS SETOF auth_token AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT *
               FROM auth_token
               WHERE id = _id;
END;
$$ LANGUAGE plpgsql STABLE;
