DROP DATABASE IF EXISTS expresso_payment;
CREATE DATABASE expresso_payment;
CREATE USER expresso_payment WITH password 'password';
GRANT ALL privileges ON DATABASE expresso_payment TO expresso_payment;

ALTER USER expresso_payment SET TIMEZONE TO 'UTC';

-- select database expresso and install extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION pgcrypto;