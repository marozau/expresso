CREATE OR REPLACE FUNCTION users_create(_email    TEXT,
                                        _locale   TEXT,
                                        _timezone INT)
  RETURNS users AS $$
DECLARE
  _user users;
BEGIN

  INSERT INTO users (status, roles, locale, timezone) VALUES ('NEW', '{USER}', _locale, _timezone)
  RETURNING *
    INTO _user;

  INSERT INTO user_profiles (user_id, email) VALUES (_user.id, _email);

  RETURN _user;

  EXCEPTION WHEN unique_violation
  THEN
    SELECT *
    INTO _user
    FROM users
    WHERE id = (SELECT user_id
                FROM user_profiles
                WHERE email = _email);

    RETURN _user;
END;
$$ LANGUAGE plpgsql;
