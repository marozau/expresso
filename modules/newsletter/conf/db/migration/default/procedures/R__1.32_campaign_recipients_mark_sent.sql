CREATE OR REPLACE FUNCTION campaign_recipients_mark_sent(_user_id BIGINT, _edition_id BIGINT)
  RETURNS SETOF campaign_recipients AS $$
DECLARE
BEGIN
  IF NOT exists(SELECT *
                FROM campaign_recipients
                WHERE user_id = _user_id AND edition_id = _edition_id)
  THEN
    RAISE '<ERROR>code=RECIPIENT_NOT_FOUND,message=campaign does not contain such recipient<ERROR>';
  END IF;

  RETURN QUERY UPDATE campaign_recipients
  SET status = 'SENT'
  WHERE user_id = _user_id AND edition_id = _edition_id
  RETURNING *;
END
$$ LANGUAGE plpgsql;