CREATE OR REPLACE FUNCTION editions_update(
  _user_id    BIGINT,
  _edition_id BIGINT,
  _date       TEXT,
  _url        TEXT,
  _title      TEXT,
  _header     JSONB,
  _footer     JSONB,
  _options    JSONB)
  RETURNS editions AS $$
DECLARE
  _edition editions;
BEGIN
  PERFORM edition_writers_validate_permissions(_user_id, _edition_id);

  UPDATE editions
  SET
    date    = coalesce(_date, date),
    url     = coalesce(_url, url),
    title   = coalesce(_title, title),
    header  = coalesce(_header, header),
    footer  = coalesce(_footer, footer),
    options = coalesce(_options, options)
  WHERE id = _edition_id
  RETURNING *
    INTO _edition;

  IF NOT FOUND
    THEN
      RAISE '<ERROR>code=EDITION_NOT_FOUND,message=edition not found with such id<ERROR>';
  END IF;

  RETURN _edition;
END;
$$ LANGUAGE plpgsql;
