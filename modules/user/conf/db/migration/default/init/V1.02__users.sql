CREATE TYPE user_status AS ENUM ('NEW', 'VERIFIED', 'BLOCKED');

CREATE TYPE user_role AS ENUM ('USER', 'READER', 'MEMBER', 'WRITER', 'EDITOR', 'CHIEF_EDITOR', 'ADMIN', 'API');

CREATE TABLE users (
  id                 BIGSERIAL PRIMARY KEY,
  status             user_status NOT NULL,
  roles              user_role[] NOT NULL,
  locale             TEXT,
  timezone           INT,
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

CREATE INDEX users_modified_timestamp_idx
  ON users (modified_timestamp);

ALTER SEQUENCE users_id_seq RESTART WITH 10000001;

