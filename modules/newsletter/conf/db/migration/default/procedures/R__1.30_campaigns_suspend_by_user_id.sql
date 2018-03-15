CREATE OR REPLACE FUNCTION campaigns_suspend_by_user_id(_user_id BIGINT, forced BOOLEAN)
  RETURNS SETOF CAMPAIGNS AS $$
DECLARE
BEGIN
-- FIXME: cannot lock row because of "PSQLException, with message: ERROR: query has no destination for result data"
--   SELECT *
--   FROM campaigns
--   WHERE user_id = _user_id AND status IN ('NEW', 'PENDING', 'SENDING', 'SUSPENDED_PENDING', 'SUSPENDED_SENDING')
--   FOR UPDATE;

  RETURN QUERY UPDATE campaigns
  SET status = ('FORCED_SUSPENDED_' || replace(status :: TEXT, 'SUSPENDED_', '')) :: CAMPAIGN_STATUS
  WHERE user_id = _user_id
        AND status IN ('NEW', 'PENDING', 'SENDING', 'SUSPENDED_PENDING', 'SUSPENDED_SENDING')
  RETURNING *;
END
$$ LANGUAGE plpgsql;