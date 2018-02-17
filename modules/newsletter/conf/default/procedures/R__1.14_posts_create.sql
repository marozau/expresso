CREATE OR REPLACE FUNCTION posts_create(
  _user_id       BIGINT,
  _edition_id    BIGINT,
  _edition_order INT,
  _title         TEXT,
  _annotation    TEXT,
  _body          JSONB,
  _options       JSONB)
  RETURNS posts AS $$
DECLARE
  _post posts;
BEGIN
  PERFORM edition_writers_validate_permissions(_user_id, _edition_id);

  INSERT INTO posts (user_id, edition_id, edition_order, title, title_url, annotation, body, options)
  VALUES (_user_id, _edition_id, _edition_order, _title, _annotation, _body, _options)
  RETURNING *
    INTO _post;

  RETURN _post;

  --TODO: order also can throw exception
  EXCEPTION WHEN unique_violation
  THEN
    RAISE '<ERROR>code=POST_ALREADY_EXISTS,message=post title is not unique<ERROR>';
END;
$$ LANGUAGE plpgsql;
