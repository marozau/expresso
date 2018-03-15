CREATE OR REPLACE FUNCTION campaigns_start_sending(_user_id BIGINT, _edition_id BIGINT)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
BEGIN
  _campaign = campaigns_lock(_edition_id);
  PERFORM newsletters_owner_validate_permissions(_user_id, _campaign.newsletter_id);

  IF _campaign.status <> 'PENDING'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot set SENDING status - campaign status is ''%''<ERROR>', _campaign.status;
  END IF;

  UPDATE campaigns
  SET status = 'SENDING'
  WHERE edition_id = _edition_id
  RETURNING *
    INTO _campaign;

  PERFORM campaign_recipients_start_sending(_campaign.newsletter_id, _edition_id);

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql;