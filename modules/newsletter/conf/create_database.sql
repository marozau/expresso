DROP DATABASE IF EXISTS expresso_newsletter;
CREATE DATABASE expresso_newsletter;
CREATE USER expresso_newsletter WITH password 'password';
GRANT ALL privileges ON DATABASE expresso_newsletter TO expresso_newsletter;

ALTER USER expresso_newsletter SET TIMEZONE TO 'UTC';

-- select database expresso and install extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION pgcrypto;