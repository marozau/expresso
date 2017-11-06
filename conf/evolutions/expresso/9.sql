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

CREATE INDEX user_login_info_user_id_idx
  ON user_login_info (user_id);
CREATE INDEX user_login_info_login_info_id_idx
  ON user_login_info (login_info_id);

CREATE TABLE password_info (
  login_info_id BIGINT NOT NULL REFERENCES login_info (id),
  hasher        TEXT   NOT NULL,
  password      TEXT   NOT NULL,
  salt          TEXT
);

CREATE INDEX password_info_login_info_id_idx
  ON password_info (login_info_id);

CREATE TABLE auth_token (
  id      UUID        NOT NULL PRIMARY KEY,
  user_id BIGINT      NOT NULL REFERENCES users (id),
  expiry  TIMESTAMPTZ NOT NULL
);

CREATE INDEX auth_token_id_and_expiry_idx
  ON auth_token (id, expiry);
CREATE INDEX auth_token_expiry_idx
  ON auth_token (expiry);


INSERT INTO users (id, email, status, roles) VALUES (0, 'admin@expresso.today', 'VERIFIED', '{ADMIN, WRITER, EDITOR}');
INSERT INTO login_info (id, provider_id, provider_key) VALUES (0, 'credentials', 'admin@expresso.today');
INSERT INTO user_login_info (user_id, login_info_id) VALUES (0, 0);
INSERT INTO password_info (login_info_id, hasher, password) VALUES (0, 'bcrypt', '$2a$10$fcdIUoXF/ruj1HcPz10eKODDyF/VKXc3kHfj144RhwltpeRXaL3J2');


# --- !Downs

DROP TABLE password_info;
DROP TABLE user_login_info;
DROP TABLE login_info;
DROP TABLE auth_token;
DELETE FROM users
WHERE id = 0;
