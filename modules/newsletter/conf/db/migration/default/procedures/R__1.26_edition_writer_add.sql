CREATE OR REPLACE FUNCTION edition_writer_add(
  _user_id     BIGINT,
  _edition_id  BIGINT,
  _new_user_id BIGINT
)
  RETURNS edition_writers AS $$
DECLARE
  _edition_writer edition_writers;
BEGIN
  PERFORM edition_validate_permissions(_user_id, _edition_id);

  INSERT INTO edition_writers (edition_id, user_id) VALUES (_edition_id, _new_user_id)
  RETURNING *
    INTO _edition_writer;

  RETURN _edition_writer;

  EXCEPTION WHEN unique_violation
  THEN
    SELECT *
    INTO _edition_writer
    FROM edition_writers
    WHERE edition_id = _edition_id AND user_id = _news_user_id;
    RETURN _edition_writer;
END;
$$ LANGUAGE plpgsql;