CREATE OR REPLACE FUNCTION newsletters_update(
  _user_id       BIGINT,
  _newsletter_id BIGINT,
  _locale        LOCALE,
  _logo_url      TEXT,
  _avatar_url    TEXT,
  _options       JSONB)
  RETURNS newsletters AS $$
DECLARE
  _newsletter newsletters;
BEGIN
  PERFORM newsletters_owner_validate_permissions(_user_id, _newsletter_id);

  UPDATE newsletters
  SET
    locale     = coalesce(_locale, locale),
    logo_url   = coalesce(_logo_url, logo_url),
    avatar_url = coalesce(_avatar_url, avatar_url),
    options    = coalesce(_options, options)
  WHERE id = _newsletter_id
  RETURNING *
    INTO _newsletter;

  RETURN _newsletter;
END;
$$ LANGUAGE plpgsql;
