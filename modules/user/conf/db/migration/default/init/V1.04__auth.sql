CREATE TABLE login_info (
  id           BIGSERIAL NOT NULL PRIMARY KEY,
  provider_id  TEXT      NOT NULL,
  provider_key TEXT      NOT NULL
);

CREATE UNIQUE INDEX login_info_provider_id_provider_key_idx
  ON login_info (provider_id, provider_key);


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


INSERT INTO users (id, status, roles, timezone) VALUES (0, 'VERIFIED', '{USER, READER, MEMBER, WRITER, EDITOR, CHIEF_EDITOR, ADMIN, API}', 3);
INSERT INTO user_profiles (user_id, email) VALUES (0, 'admin@expresso.today');
INSERT INTO login_info (id, provider_id, provider_key) VALUES (0, 'credentials', '919e1bc4370bd99e874a538970836e4b0f6fc88240c71fb2f8e3e7b98f448f40');
INSERT INTO user_login_info (user_id, login_info_id) VALUES (0, 0);
INSERT INTO password_info (login_info_id, hasher, password) VALUES (0, 'bcrypt', '$2a$10$XHcP1eiVrVpGTfpXQUGSJ.F9VnF.adwQ75fRPep0D4ObpoM408lp6');
