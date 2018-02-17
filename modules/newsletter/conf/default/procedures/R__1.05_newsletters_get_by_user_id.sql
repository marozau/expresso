CREATE OR REPLACE FUNCTION newsletters_get_by_user_id(_user_id BIGINT)
  RETURNS SETOF newsletters AS $$
BEGIN
  RETURN QUERY SELECT *
               FROM newsletters
               WHERE user_id = _user_id OR (id IN (SELECT newsletter_id
                                                   FROM newsletter_writers
                                                   WHERE user_id = _user_id));
END;
$$ LANGUAGE plpgsql STABLE;
