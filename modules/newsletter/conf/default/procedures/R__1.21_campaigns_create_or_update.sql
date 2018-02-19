CREATE OR REPLACE FUNCTION campaigns_create_or_update(
  _edition_id BIGINT,
  _send_time  TIMESTAMPTZ,
  _preview    TEXT,
  _options    JSONB
)
  RETURNS campaigns AS $$
DECLARE
  _edition  editions;
  _campaign campaigns;
BEGIN
  IF _send_time - INTERVAL '15 minutes' < CURRENT_TIMESTAMP
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_SCHEDULE,message=send timestamp must at leas 15 minutes in the future<ERROR>';
  END IF;

  SELECT *
  INTO _edition
  FROM editions
  WHERE id = _edition_id;

  IF NOT FOUND
  THEN
    RAISE '<ERROR>code=EDITION_NOT_FOUND,message=edition not found with such id ''%''<ERROR>', _edition_id;
  END IF;

  _campaign = campaigns_lock(_edition_id);

  IF _campaign.status <> 'NEW'
  THEN
    RAISE '<ERROR>code=INVALID_CAMPAIGN_STATUS,message=cannot update, campaign status is ''$''<ERROR>', _campaign.status;
  END IF;

  INSERT INTO campaigns (edition_id, newsletter_id, preview, status, send_time, options)
  VALUES (_edition_id, _edition.newsletter_id, _preview, _send_time, 'NEW', _options)
  ON CONFLICT (edition_id)
    DO UPDATE
      SET preview = coalesce(_preview, preview),
        send_time = coalesce(_send_time, send_time),
        options   = coalesce(_options, options)
  RETURNING *
    INTO _campaign;

  RETURN _campaign;

END;
$$ LANGUAGE plpgsql;