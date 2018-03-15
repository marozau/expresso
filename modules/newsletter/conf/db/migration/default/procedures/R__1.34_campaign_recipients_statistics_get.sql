CREATE OR REPLACE FUNCTION campaign_recipients_statistics_get(_edition_id BIGINT)
  RETURNS TABLE(edition_id BIGINT, count INT, sending INT, sent INT, failed INT) AS $$
DECLARE
BEGIN
  RETURN QUERY
  WITH all_recipients AS (
      SELECT *
      FROM campaign_recipients
      WHERE campaign_recipients.edition_id = _edition_id
  ), sending AS (
      SELECT *
      FROM campaign_recipients
      WHERE campaign_recipients.edition_id = _edition_id AND status <> 'SENT'
  ), sent AS (
      SELECT *
      FROM campaign_recipients
      WHERE campaign_recipients.edition_id = _edition_id AND status = 'SENT'
  ), failed AS (
      SELECT *
      FROM campaign_recipients
      WHERE campaign_recipients.edition_id = _edition_id AND status <> 'SENT' AND attempts > 0
  ) SELECT
      _edition_id,
      t1.cnt :: INT,
      t2.cnt :: INT,
      t3.cnt :: INT,
      t4.cnt :: INT
    FROM
      (SELECT count(*) AS cnt
       FROM all_recipients) AS t1,
      (SELECT count(*) AS cnt
       FROM sending) AS t2,
      (SELECT count(*) AS cnt
       FROM sent) AS t3,
      (SELECT count(*) AS cnt
       FROM failed) AS t4;

END
$$ LANGUAGE plpgsql STABLE;