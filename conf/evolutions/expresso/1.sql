# --- !Ups

CREATE OR REPLACE FUNCTION update_last_modified_timestamp()
  RETURNS TRIGGER AS $$
BEGIN
  IF NEW != OLD
  THEN
    NEW.modified_timestamp := timezone('UTC', now());;
    NEW.created_timestamp := OLD.created_timestamp;;
  END IF;;
  RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_create_timestamp()
  RETURNS TRIGGER AS $$
BEGIN
  IF NEW.created_timestamp ISNULL
  THEN
    NEW.created_timestamp := timezone('UTC', now());;
  END IF;;
  IF NEW.modified_timestamp ISNULL
  THEN
    NEW.modified_timestamp := timezone('UTC', now());;
  END IF;;
RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;


# --- !Downs

DROP FUNCTION IF EXISTS update_last_modified_timestamp();
