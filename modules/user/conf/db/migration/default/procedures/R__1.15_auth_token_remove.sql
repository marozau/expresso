CREATE OR REPLACE FUNCTION auth_token_remove(_id UUID)
  RETURNS VOID AS $$
DECLARE
BEGIN
  DELETE FROM auth_token
  WHERE id = _id;
END;
$$ LANGUAGE plpgsql;
