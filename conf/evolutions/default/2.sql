# --- !Ups

DROP TYPE IF EXISTS user_status CASCADE;
CREATE TYPE user_status AS ENUM ('NEW', 'VERIFIED', 'BLOCKED');
DROP TYPE IF EXISTS user_role CASCADE;
CREATE TYPE user_role AS ENUM ('USER', 'WRITER', 'EDITOR', 'ADMIN');

CREATE TABLE users (
  id                 BIGSERIAL PRIMARY KEY,
  email              TEXT        NOT NULL UNIQUE,
  status             user_status NOT NULL,
  roles              user_role[] NOT NULL,
  reason             TEXT,
  created_timestamp  TIMESTAMPTZ DEFAULT timezone('UTC', now()),
  modified_timestamp TIMESTAMPTZ DEFAULT timezone('UTC', now())
);

DROP TRIGGER IF EXISTS trigger_user_modified
ON users;
CREATE TRIGGER trigger_user_modified
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();

DROP TRIGGER IF EXISTS trigger_users_created
ON users;
CREATE TRIGGER trigger_users_created
BEFORE INSERT ON users
FOR EACH ROW
EXECUTE PROCEDURE update_create_timestamp();

CREATE INDEX users_modified_timestamp_idx
  ON users (modified_timestamp);

CREATE INDEX users_email_idx
  ON users (email);

ALTER SEQUENCE users_id_seq RESTART WITH 10000001;

# --- !Downs

DROP TABLE IF EXISTS users CASCADE;