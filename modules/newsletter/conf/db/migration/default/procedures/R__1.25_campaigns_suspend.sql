CREATE OR REPLACE FUNCTION campaigns_suspend(_user_id BIGINT, _edition_id BIGINT)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
BEGIN
  _campaign = campaigns_lock(_edition_id);
  PERFORM newsletters_owner_validate_permissions(_user_id, _campaign.newsletter_id);

  IF _campaign.status <> 'PENDING' AND _campaign.status <> 'SENDING'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot suspend - campaign status is ''%''<ERROR>', _campaign.status;
  END IF;

  UPDATE campaigns
  SET status = ('SUSPENDED_' || _campaign.status) :: CAMPAIGN_STATUS
  WHERE edition_id = _edition_id
  RETURNING *
    INTO _campaign;

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql;