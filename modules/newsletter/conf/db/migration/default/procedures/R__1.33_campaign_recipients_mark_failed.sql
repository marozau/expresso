CREATE OR REPLACE FUNCTION campaign_recipients_mark_failed(_user_id BIGINT, _edition_id BIGINT, _reason TEXT)
  RETURNS campaign_recipients AS $$
DECLARE
  _recipient campaign_recipients;
BEGIN
  SELECT *
  INTO _recipient
  FROM campaign_recipients
  WHERE user_id = _user_id AND edition_id = _edition_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=RECIPIENT_NOT_FOUND,message=campaign does not contain such recipient<ERROR>';
  END IF;

  UPDATE campaign_recipients
  SET
    reason   = _reason,
    attempts = attempts + 1
  WHERE user_id = _user_id AND edition_id = _edition_id
  RETURNING *
    INTO _recipient;

  RETURN _recipient;
END
$$ LANGUAGE plpgsql;