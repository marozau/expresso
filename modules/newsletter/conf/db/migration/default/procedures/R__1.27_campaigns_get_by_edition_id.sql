CREATE OR REPLACE FUNCTION campaigns_get_by_edition_id(_user_id BIGINT, _edition_id BIGINT)
  RETURNS campaigns AS $$
DECLARE
  _campaign campaigns;
BEGIN
  PERFORM edition_writers_validate_permissions(_user_id, _edition_id);

  SELECT *
  INTO _campaign
  FROM campaigns
  WHERE edition_id = _edition_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=CAMPAIGN_NOT_FOUND,message=campaign not found for edition_id:%<ERROR>', _edition_id;
  END IF;

  RETURN _campaign;
END
$$ LANGUAGE plpgsql STABLE;