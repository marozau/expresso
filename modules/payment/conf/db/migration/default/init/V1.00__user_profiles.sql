--TODO: encrypt this table

CREATE TYPE user_status AS ENUM ('NEW', 'VERIFIED', 'BLOCKED');

CREATE TABLE user_profiles (
  user_id            BIGINT PRIMARY KEY,

  status             user_status,

  first_name         TEXT,
  last_name          TEXT,
  date_of_birth      DATE,
  country            CHAR(2),
  city               TEXT,
  postcode           TEXT,

  modified_timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX user_profiles_modified_timestamp_idx
  ON user_profiles (modified_timestamp);

DROP TRIGGER IF EXISTS user_profiles_modified
ON user_profiles;
CREATE TRIGGER user_profiles_modified
BEFORE UPDATE ON user_profiles
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();


