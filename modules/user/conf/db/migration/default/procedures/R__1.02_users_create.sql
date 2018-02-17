CREATE OR REPLACE FUNCTION users_create(_email    TEXT,
                                        _locale   TEXT,
                                        _timezone INT)
  RETURNS users AS $$
DECLARE
  _user users;
BEGIN

  INSERT INTO users (email, status, roles, locale, timezone) VALUES (_email, 'NEW', '{USER}', _locale, _timezone)
  RETURNING *
    INTO _user;

  INSERT INTO user_profiles (user_id) VALUES (_user.id);

  RETURN _user;

  EXCEPTION WHEN unique_violation
  THEN
    SELECT *
    INTO _user
    FROM users
    WHERE email = _email;

    RETURN _user;
END;
$$ LANGUAGE plpgsql;
