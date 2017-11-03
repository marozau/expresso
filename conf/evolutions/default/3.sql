# --- !Ups

DROP TYPE IF EXISTS user_sex;
CREATE TYPE user_sex AS ENUM ('MALE', 'FEMALE', 'UNKNOWN');

CREATE TABLE user_profiles (
  user_id            BIGINT PRIMARY KEY REFERENCES users (id),

  timezone           INT,
  locale             TEXT,
  first_name         TEXT,
  last_name          TEXT,
  sex                user_sex,
  date_of_birth      DATE,
  country            CHAR(2),
  city               TEXT,
  postcode           TEXT,

  rating             DECIMAL,

  modified_timestamp TIMESTAMPTZ DEFAULT timezone('UTC', now())
);

CREATE INDEX user_profiles_modified_timestamp_idx
  ON user_profiles (modified_timestamp);

DROP TRIGGER IF EXISTS user_profiles_modified
ON user_profiles;
CREATE TRIGGER user_profiles_modified
BEFORE UPDATE ON user_profiles
FOR EACH ROW
EXECUTE PROCEDURE update_last_modified_timestamp();


# --- !Downs

DROP TABLE IF EXISTS user_profiles CASCADE;
