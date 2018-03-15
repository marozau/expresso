CREATE OR REPLACE FUNCTION posts_update(
  _user_id       BIGINT,
  _post_id       BIGINT,
  _edition_order INT,
  _title         TEXT,
  _annotation    TEXT,
  _body          JSONB,
  _options       JSONB)
  RETURNS POSTS AS $$
DECLARE
  _post POSTS;
BEGIN
  --TODO: validate permissions

  UPDATE posts
  SET
    edition_order = coalesce(_edition_order, edition_order),
    title         = coalesce(_title, title),
    annotation    = coalesce(_annotation, annotation),
    body          = coalesce(_body, body),
    options       = coalesce(_options, options)
  WHERE id = _post_id
  RETURNING *
    INTO _post;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=EDITION_NOT_FOUND,message=edition not found with such id<ERROR>';
  END IF;

  RETURN _post;
END;
$$ LANGUAGE plpgsql;
