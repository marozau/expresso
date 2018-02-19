CREATE OR REPLACE FUNCTION recipients_update_status(_recipient_id UUID, _status RECIPIENT_STATUS)
  RETURNS recipients AS $$
DECLARE
  _recipient recipients;
BEGIN
  IF _status = 'PENDING'
    THEN
      RAISE '<ERROR>code=INVALID_RECIPIENT_STATUS,message=% status update is not permitted<ERROR>', _status;
  END IF;

  SELECT *
  INTO _recipient
  FROM recipients
  WHERE id = _recipient_id
  FOR UPDATE;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=RECIPIENT_NOT_FOUND,message=no such recipient_id:''%''<ERROR>', _recipient_id;
  END IF;

  UPDATE recipients
  SET status = _status
  WHERE id = _recipient_id
  RETURNING *
    INTO _recipient;

  RETURN _recipient;
END;
$$ LANGUAGE plpgsql;