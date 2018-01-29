# --- !Ups

CREATE UNIQUE INDEX editions_newsletter_date_id_idx
  ON editions (newsletter_id, date);

# --- !Downs