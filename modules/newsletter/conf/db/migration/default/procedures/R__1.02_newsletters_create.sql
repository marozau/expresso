CREATE OR REPLACE FUNCTION newsletters_create(
  _user_id BIGINT,
  _name    TEXT,
  _locale  LOCALE)
  RETURNS newsletters AS $$
DECLARE
  _newsletter newsletters;
BEGIN
  --TODO: user cannot change newsletter name
  --TODO: free user can have no more than 1 newsletter
  --TODO: change _name_url is a premium feature
  --TODO: change _logo_url is a premium feature
  --TODO: change _avatar_url is a premium feature
  INSERT INTO newsletters (user_id, name, locale)
  VALUES (_user_id, _name, _locale)
  RETURNING *
    INTO _newsletter;

  RETURN _newsletter;

  EXCEPTION WHEN unique_violation
  THEN
    RAISE '<ERROR>code=NEWSLETTER_ALREADY_EXISTS,message=name ''%'' is already registered<ERROR>', _name;
END;
$$ LANGUAGE plpgsql;
