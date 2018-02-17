CREATE OR REPLACE FUNCTION editions_get_by_id(_user_id BIGINT, _edition_id BIGINT)
  RETURNS editions AS $$
DECLARE
  _edition editions;
BEGIN
  PERFORM edition_writers_validate_permissions(_user_id, _edition_id);

  SELECT *
  INTO _edition
  FROM editions
  WHERE id = _edition_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=EDITION_NOT_FOUND,message=edition not found with such id ''%''<ERROR>', _edition_id;
  END IF;

  RETURN _edition;
END;
$$ LANGUAGE plpgsql STABLE;
