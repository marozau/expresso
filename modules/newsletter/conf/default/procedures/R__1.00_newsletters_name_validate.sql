CREATE OR REPLACE FUNCTION newsletters_name_validate(_name TEXT)
  RETURNS BOOLEAN AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT EXISTS(SELECT *
                             FROM newsletters
                             WHERE name = _name);
END;
$$ LANGUAGE plpgsql STABLE;
