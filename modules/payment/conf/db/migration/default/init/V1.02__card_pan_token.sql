-- recommended by PICDSS to use separate table PAN_Token just for pan to token relation.
-- allowed only to keep encrypted PAN, expiration date and cardholder name in the pcidss scope
-- not allowed to keep together masked and hashed PAN
-- not allowed any correlation between masked and hashed PAN
-- cards shouldn't be kept without need:
-- a) marked as 'deleted' by user should be removed
-- (it's kept for refund or for further withdrawal, so only card without deposit is removed)
-- b) expired cards should be removed
-- token should be at least 256bit to avoid collision
DROP TABLE IF EXISTS card_pan_token;

CREATE TABLE card_pan_token (
  token TEXT PRIMARY KEY,
  pan   TEXT NOT NULL
);
