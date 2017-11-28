DROP DATABASE IF EXISTS expresso;
CREATE DATABASE expresso;
CREATE USER expresso WITH password 'password';
GRANT ALL privileges ON DATABASE expresso TO expresso;

ALTER USER expresso SET TIMEZONE TO 'UTC';

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION pgcrypto;