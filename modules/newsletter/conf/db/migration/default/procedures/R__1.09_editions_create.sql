CREATE OR REPLACE FUNCTION editions_create(
  _user_id       BIGINT,
  _newsletter_id BIGINT,
  _date          DATE)
  RETURNS editions AS $$
DECLARE
  _editions editions;
BEGIN
  PERFORM newsletters_owner_validate_permissions(_user_id, _newsletter_id);

  INSERT INTO editions (newsletter_id, date)
  VALUES (_newsletter_id, _date)
  RETURNING *
    INTO _editions;

  RETURN _editions;

  EXCEPTION WHEN unique_violation
  THEN
    RAISE '<ERROR>code=EDITION_ALREADY_EXISTS,message=edition for the date is not unique, date ''%''<ERROR>', _date;
END;
$$ LANGUAGE plpgsql;
