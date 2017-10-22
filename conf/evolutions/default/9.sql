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

CREATE TABLE auth_token (
  id      UUID        NOT NULL,
  user_id BIGINT      NOT NULL REFERENCES users (id),
  expiry  TIMESTAMPTZ NOT NULL
);

INSERT INTO users (id, email, status, roles) VALUES (10000000, 'admin@expresso.today', 'VERIFIED', '{ADMIN}');
INSERT INTO login_info (id, provider_id, provider_key) VALUES (0, 'credentials', 'admin@expresso.today');
INSERT INTO user_login_info (user_id, login_info_id) VALUES (10000000, 0);
INSERT INTO password_info (login_info_id, hasher, password) VALUES (0, 'bcrypt', '$2a$10$fcdIUoXF/ruj1HcPz10eKODDyF/VKXc3kHfj144RhwltpeRXaL3J2');


# --- !Downs

DROP TABLE password_info;
DROP TABLE user_login_info;
DROP TABLE login_info;
DROP TABLE auth_token;
DELETE FROM users
WHERE id = 10000000;
