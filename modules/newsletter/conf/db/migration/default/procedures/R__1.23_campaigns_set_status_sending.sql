CREATE OR REPLACE FUNCTION campaigns_set_status_sending(_edition_id BIGINT)
  RETURNS CAMPAIGNS AS $$
DECLARE
  _campaign CAMPAIGNS;
BEGIN
  _campaign = campaigns_lock(_edition_id);

  IF _campaign.status <> 'PENDING'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot set SENDING status, campaign status is ''%''<ERROR>', _campaign.status;
  END IF;

  UPDATE campaigns
  SET status = 'SENDING'
  WHERE edition_id = _edition_id
  RETURNING *
    INTO _campaign;

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql;