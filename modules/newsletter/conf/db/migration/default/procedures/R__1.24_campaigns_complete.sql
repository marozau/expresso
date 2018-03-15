CREATE OR REPLACE FUNCTION campaigns_complete(_user_id BIGINT, _edition_id BIGINT, _forced BOOLEAN)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
BEGIN
  _campaign = campaigns_lock(_edition_id);
  PERFORM newsletters_owner_validate_permissions(_user_id, _campaign.newsletter_id);

  IF _campaign.status <> 'SENDING'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot set SENT status - campaign status is ''%''<ERROR>', _campaign.status;
  END IF;

  IF NOT _forced AND exists(SELECT *
                           FROM campaign_recipients
                           WHERE edition_id = _edition_id AND status <> 'SENT')
  THEN
    RETURN _campaign;
  END IF;

  UPDATE campaigns
  SET status = 'SENT'
  WHERE edition_id = _edition_id
  RETURNING *
    INTO _campaign;

  IF _forced
  THEN
    UPDATE campaign_recipients
    SET status = 'SENT'
    WHERE edition_id = _edition_id;
  END IF;

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql;