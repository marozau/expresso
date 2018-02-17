CREATE TABLE newsletter_writers (
  id                UUID PRIMARY KEY          DEFAULT uuid_generate_v4(),
  newsletter_id     BIGINT      NOT NULL REFERENCES newsletters (id),
  user_id           BIGINT      NOT NULL,
  created_timestamp TIMESTAMPTZ NOT NULL      DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX newsletter_writers_newsletter_id_user_id_unique_idx
  ON newsletter_writers (newsletter_id, user_id);

CREATE TABLE edition_writers (
  id                UUID PRIMARY KEY          DEFAULT uuid_generate_v4(),
  edition_id        BIGINT      NOT NULL REFERENCES editions (id),
  user_id           BIGINT      NOT NULL,
  created_timestamp TIMESTAMPTZ NOT NULL      DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX edition_writers_edition_id_user_id_unique_idx
  ON edition_writers (edition_id, user_id);