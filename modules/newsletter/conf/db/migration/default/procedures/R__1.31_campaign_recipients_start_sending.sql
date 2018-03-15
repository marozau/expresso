CREATE OR REPLACE FUNCTION campaign_recipients_start_sending(_newsletter_id BIGINT, _edition_id BIGINT)
  RETURNS SETOF CAMPAIGN_RECIPIENTS AS $$
DECLARE
BEGIN
  IF NOT exists(SELECT *
                FROM recipients
                WHERE newsletter_id = _newsletter_id AND status = 'SUBSCRIBED')
  THEN
    RAISE '<ERROR>code=RECIPIENT_NOT_FOUND,message=campaign does not contain any recipient<ERROR>';
  END IF;

  RETURN QUERY INSERT INTO campaign_recipients (user_id, recipient_id, edition_id, status)
    SELECT
      user_id,
      id,
      _edition_id,
      'SENDING'
    FROM recipients
    WHERE newsletter_id = _newsletter_id AND status = 'SUBSCRIBED'
  RETURNING *;

  EXCEPTION WHEN unique_violation
  THEN
    RETURN QUERY SELECT *
                 FROM campaign_recipients
                 WHERE edition_id = _edition_id AND status = 'SENDING';
END
$$ LANGUAGE plpgsql;