CREATE OR REPLACE FUNCTION ums_user_register_password(_email                        TEXT,
                                                      _password                     TEXT,
                                                      _locale                       TEXT,
                                                      _timezone                     TEXT)
  RETURNS users.id%TYPE AS $$
DECLARE
BEGIN
  RETURN 0;
END;
$$ LANGUAGE plpgsql;
