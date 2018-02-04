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
  _user = users_create(_email, _locale, _timezone);

  INSERT INTO login_info (provider_id, provider_key) VALUES ('credentials', _provider_key)
  RETURNING id
    INTO _login_info_id;

  INSERT INTO user_login_info (user_id, login_info_id) VALUES (_user.id, _login_info_id);

  INSERT INTO password_info (login_info_id, hasher, password) VALUES (_login_info_id, _hasher, _password);
  RETURN _user;

  EXCEPTION WHEN unique_violation
  THEN
    RAISE '<ERROR>code=USER_ALREADY_EXISTS,message=email ''%'' is already registered<ERROR>', _email;
END;
$$ LANGUAGE plpgsql;
