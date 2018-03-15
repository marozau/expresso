CREATE OR REPLACE FUNCTION update_last_modified_timestamp()
  RETURNS TRIGGER AS $$
BEGIN
  IF NEW != OLD
  THEN
    NEW.modified_timestamp := CURRENT_TIMESTAMP;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;