CREATE OR REPLACE FUNCTION newsletters_name_url_validate(_name_url TEXT)
  RETURNS BOOLEAN AS $$
DECLARE
BEGIN
  RETURN QUERY SELECT EXISTS(SELECT *
                             FROM newsletters
                             WHERE name_url = _name_url);
END;
$$ LANGUAGE plpgsql STABLE;
