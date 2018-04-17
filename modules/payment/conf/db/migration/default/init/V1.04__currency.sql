CREATE TABLE currency (
  code             CHAR(5) PRIMARY KEY,
  numeric_code     INTEGER NOT NULL,
  symbol           TEXT    NOT NULL,
  name             TEXT    NOT NULL,
  unit             INTEGER NOT NULL,
  account_currency BOOLEAN NOT NULL
);

insert into currency (code, numeric_code, symbol, name, unit, account_currency) values
  ('EUR', 978, '€', 'Euro', 2, true),
  ('USD', 840, '$', 'US Dollar', 2, true),
  ('GBP', 826, '£', 'Pound Sterling', 2, true),
  ('SEK', 752, '&#107;&#114;', 'Swedish Krona', 2, false),
  ('BGN', 975, '&#1083;&#1074;', 'Bulgarian Lev', 2, false),
  ('HUF', 348, '&#70;&#116;', 'Forint', 2, false),
  ('DKK', 208, '&#107;&#114;', 'Danish Krone', 2, false),
  ('PLN', 985, 'zł', 'Zloty', 2, false),
  ('RON', 946, '&#108;&#101;&#105;', 'Romanian Leu', 2, false),
  ('CZK', 203, '&#75;&#269;', 'Czech Koruna', 2, false),
  ('CAD', 124, 'C$', 'Canadian Dollar', 2, false),
  ('NOK', 578, 'kr', 'Norwegian Krone', 2, false),
  ('RUB', 643, '₽', 'Russian Ruble', 2, false),
  ('CNY', 156, '&#x00a5;', 'Yuan Renminbi', 2, false),
  ('CHF', 756, 'Fr.', 'Swiss Franc', 2, false),
  ('NZD', 554, 'NZ$', 'New Zealand Dollar', 2, false),
  ('INR', 356, '&#x20b9;', 'Indian Rupee', 2, false),
  ('ILS', 376, '&#x20aa;', 'New Israeli Sheqel', 2, false),
  ('TRY', 949, 'TL', 'Turkish Lira', 2, false),
  ('MXN', 484, 'Mex$', 'Mexican Peso', 2, false),
  ('AUD', 36, 'A$', 'Australian Dollar', 2, false),
  ('ZAR', 710, 'R', 'Rand', 2, false),
  ('SGD', 702, 'S$', 'Singapore Dollar', 2, false),
  ('JPY', 392, '&#165;', 'Yen', 0, false),
  ('HKD', 344, 'HK$', 'Hong Kong Dollar', 2, false),
  ('GBX', 1010, 'p', 'Penny Sterling', 2, false),
  ('BTC', 0, 'Ƀ', 'Bitcoin', 8, false)