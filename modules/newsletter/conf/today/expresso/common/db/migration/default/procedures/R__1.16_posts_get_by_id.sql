CREATE OR REPLACE FUNCTION posts_get_by_id(_user_id BIGINT, _post_id BIGINT)
  RETURNS posts AS $$
DECLARE
  _post posts;
BEGIN
  -- TODO: validate permissions

  SELECT *
  INTO _post
  FROM posts
  WHERE id = _post_id;

  RETURN _post;
END;
$$ LANGUAGE plpgsql STABLE;
