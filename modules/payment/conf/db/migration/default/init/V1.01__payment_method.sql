DROP TYPE IF EXISTS payment_option CASCADE;
DROP TYPE IF EXISTS payment_system CASCADE;
DROP TYPE IF EXISTS payment_method_status CASCADE;

CREATE TYPE payment_option AS ENUM (
  'BANK_CARD',
  'YANDEX_WALLET',
  'APPLE_PAY',
  'SBERBANK',
  'QIWI',
  'WEBMONEY',
  'CASH',
  'MOBILE_BALANCE',
  'ALFABANK');

CREATE TYPE payment_system AS ENUM (
  'YANDEX'
);

CREATE TYPE payment_method_status AS ENUM (
  'OK',
  'INREVIEW',
  'BLOCKED',
  'INCOMPLETE');

CREATE TABLE payment_method (
  id                      BIGSERIAL PRIMARY KEY,
  payment_option          payment_option        NOT NULL,
  payment_system          payment_system        NOT NULL,
  user_id                 BIGINT                NOT NULL,
  status                  payment_method_status NOT NULL,
  expiration_date         DATE,

  display_name            TEXT,
  is_deleted              BOOLEAN               NOT NULL                        DEFAULT FALSE,
  is_default              BOOLEAN               NOT NULL                        DEFAULT FALSE,

  first_payment_timestamp TIMESTAMPTZ,
  last_payment_timestamp  TIMESTAMPTZ,
  last_failed_timestamp   TIMESTAMPTZ,

  details                 JSONB,

  created_timestamp       TIMESTAMPTZ           NOT NULL                        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX payment_method_user_id_index
  ON payment_method (user_id);
