CREATE OR REPLACE FUNCTION newsletters_name_validate(_name TEXT)
  RETURNS SETOF BOOLEAN AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT NOT EXISTS(SELECT *
                             FROM newsletters
                             WHERE name = _name);
END;
$$ LANGUAGE plpgsql STABLE;
