CREATE OR REPLACE FUNCTION users_create_auth_password(_email    TEXT,
                                                      _password TEXT,
                                                      _hasher   TEXT,
                                                      _locale   TEXT,
                                                      _timezone INT)
  RETURNS users AS $$
DECLARE
  _user          users;
  _login_info_id BIGINT;
  _provider_key  TEXT = encode(digest(_email, 'sha256'), 'hex');
BEGIN
  IF NOT user_profiles_is_email_exist(_email)
  THEN
    INSERT INTO users (status, roles, locale, timezone) VALUES ('NEW', '{USER}', _locale, _timezone)
    RETURNING *
      INTO _user;

    INSERT INTO user_profiles (user_id, email) VALUES (_user.id, _email);

    INSERT INTO login_info (id, provider_id, provider_key) VALUES (_user.id, 'credentials', _provider_key)
    RETURNING id
      INTO _login_info_id;

    INSERT INTO user_login_info (user_id, login_info_id) VALUES (_user.id, _login_info_id);

    INSERT INTO password_info (login_info_id, hasher, password) VALUES (_login_info_id, _hasher, _password);
    RETURN _user;
  ELSE
    RAISE '<ERROR>code=USER_ALREADY_EXISTS,message=email ''%'' is already registered<ERROR>', _email;
  END IF;
END;
$$ LANGUAGE plpgsql;
