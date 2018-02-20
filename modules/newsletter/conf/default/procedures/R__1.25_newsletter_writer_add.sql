CREATE OR REPLACE FUNCTION newsletter_writer_add(
  _user_id       BIGINT,
  _newsletter_id BIGINT,
  _new_user_id   BIGINT
)
  RETURNS newsletter_writer AS $$
DECLARE
  _newsletter_writer newsletter_writers;
BEGIN
  PERFORM newsletters_validate_permissions(_user_id, _newsletter_id);

  INSERT INTO newsletter_writers (newsletter_id, user_id) VALUES (_newsletter_id, _new_user_id)
  RETURNING *
    INTO _newsletter_writer;

  RETURN _newsletter_writer;

  EXCEPTION WHEN unique_violation
  THEN
    SELECT *
    INTO _newsletter_writer
    FROM newsletter_writers
    WHERE newsletter_id = _newsletter_id AND user_id = _news_user_id;
    RETURN _newsletter_writer;
END;
$$ LANGUAGE plgpsql;