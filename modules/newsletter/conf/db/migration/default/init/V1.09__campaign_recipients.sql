CREATE TABLE campaign_recipients (
  user_id      BIGINT          NOT NULL,
  recipient_id UUID            NOT NULL REFERENCES recipients (id),
  edition_id   BIGINT          NOT NULL REFERENCES campaigns (edition_id),
  status       CAMPAIGN_STATUS NOT NULL,
  attempts     INT             NOT NULL DEFAULT 0,
  reason       TEXT,
  PRIMARY KEY (recipient_id, edition_id)
);

CREATE INDEX campaign_recipients_user_id_idx
  ON campaign_recipients (user_id);

CREATE INDEX campaign_recipients_campaign_id_idx
  ON campaign_recipients (edition_id);

CREATE INDEX campaign_recipients_edition_id_status_idx
  ON campaign_recipients (edition_id, status);
