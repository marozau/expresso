CREATE OR REPLACE FUNCTION newsletters_get_by_id(_user_id BIGINT, _newsletter_id BIGINT)
  RETURNS newsletters AS $$
DECLARE
  _newsletter newsletters;
BEGIN
  PERFORM newsletter_writers_validate_permissions(_user_id, _newsletter_id);

  SELECT *
  INTO _newsletter
  FROM newsletters
  WHERE id = _newsletter_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=NEWSLETTER_NOT_FOUND,message=invalid newsletter_id ''%''<ERROR>', _newsletter_id;
  END IF;
END;
$$ LANGUAGE plpgsql STABLE;
