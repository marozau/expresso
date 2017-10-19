# --- !Ups

CREATE TABLE login_info (
  id           BIGSERIAL NOT NULL PRIMARY KEY,
  provider_id  TEXT      NOT NULL,
  provider_key TEXT      NOT NULL
);
CREATE TABLE user_login_info (
  user_id       BIGINT NOT NULL REFERENCES users (id),
  login_info_id BIGINT NOT NULL REFERENCES login_info (id)
);

CREATE TABLE password_info (
  login_info_id BIGINT NOT NULL REFERENCES login_info (id),
  hasher        TEXT   NOT NULL,
  password      TEXT   NOT NULL,
  salt          TEXT
);

# --- !Downs

DROP TABLE login_info;
DROP TABLE user_login_info;
DROP TABLE password_info;