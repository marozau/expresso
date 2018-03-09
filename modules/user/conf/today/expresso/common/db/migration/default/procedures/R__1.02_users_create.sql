CREATE OR REPLACE FUNCTION users_create(_email    TEXT,
                                        _locale   TEXT,
                                        _timezone INT,
                                        _roles    USER_ROLE [])
  RETURNS USERS AS $$
DECLARE
  _user USERS;
BEGIN

  INSERT INTO users (email, status, roles, locale, timezone) VALUES (_email, 'NEW', _roles, _locale, _timezone)
  RETURNING *
    INTO _user;

  INSERT INTO user_profiles (user_id) VALUES (_user.id);

  RETURN _user;

  EXCEPTION WHEN unique_violation
  THEN
    SELECT *
    INTO _user
    FROM users
    WHERE email = _email
    FOR UPDATE;

    IF _roles NOTNULL AND NOT _roles <@ _user.roles
    THEN
      UPDATE users
      SET roles = array_sort_unique(roles || _roles)
      WHERE email = _email
      RETURNING *
        INTO _user;
    END IF;

    RETURN _user;
END;
$$ LANGUAGE plpgsql;
