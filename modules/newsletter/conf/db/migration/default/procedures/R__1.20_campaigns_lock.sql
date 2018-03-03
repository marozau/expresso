CREATE OR REPLACE FUNCTION campaigns_lock(_edition_id BIGINT)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
BEGIN
  SELECT _campaign
  FROM campaigns
  WHERE edition_id = _edition_id
  FOR UPDATE;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=CAMPAIGN_NOT_FOUND,message=campaign not found for edition_id:%<ERROR>', _edition_id;
  END IF;

  RETURN _campaign;
END;
$$ LANGUAGE plpgsql STABLE;