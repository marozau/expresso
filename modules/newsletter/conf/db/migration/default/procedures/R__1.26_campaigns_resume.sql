CREATE OR REPLACE FUNCTION campaigns_resume(_user_id BIGINT, _edition_id BIGINT)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
  _status CAMPAIGN_STATUS;
BEGIN
  _campaign = campaigns_lock(_edition_id);
  PERFORM newsletters_owner_validate_permissions(_user_id, _campaign.newsletter_id);

  IF _campaign.status <> 'SUSPENDED_PENDING' AND _campaign.status <> 'SUSPENDED_SENDING'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot resume - campaign status is ''%''<ERROR>', _campaign.status;
  END IF;

  _status = replace(_campaign.status :: TEXT, 'SUSPENDED_', '') :: CAMPAIGN_STATUS;

  UPDATE campaigns
  SET status = _status
  WHERE edition_id = _edition_id
  RETURNING *
    INTO _campaign;

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql;