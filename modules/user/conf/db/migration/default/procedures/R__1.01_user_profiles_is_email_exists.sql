-- TODO: remove it
CREATE OR REPLACE FUNCTION user_profiles_is_email_exist(_email TEXT)
  RETURNS BOOLEAN AS $$
DECLARE
BEGIN
  RETURN EXISTS (SELECT 1 FROM user_profiles WHERE email = _email);
END;
$$ LANGUAGE plpgsql STABLE;
