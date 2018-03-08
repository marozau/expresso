CREATE OR REPLACE FUNCTION campaigns_set_status_pending(_user_id BIGINT, _edition_id BIGINT)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
BEGIN
  _campaign = campaigns_lock(_edition_id);
  PERFORM newsletters_owner_validate_permissions(_user_id, _campaign.newsletter_id);

  IF _campaign.status <> 'NEW'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot set PENDING status - campaign status is ''%''<ERROR>', _campaign.status;
  END IF;

  UPDATE campaigns
  SET status = 'PENDING'
  WHERE edition_id = _edition_id
  RETURNING *
    INTO _campaign;

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql;