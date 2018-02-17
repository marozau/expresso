CREATE OR REPLACE FUNCTION editions_get_by_newsletter_id(_user_id BIGINT, _newsletter_id BIGINT)
  RETURNS SETOF editions AS $$
BEGIN
  IF NOT exists(SELECT *
                FROM newsletters
                WHERE id = _newsletter_id)
  THEN
    RAISE '<ERROR>code=NEWSLETTER_NOT_FOUND,message=newsletter not found with such id ''%''<ERROR>', _newsletter_id;
  END IF;

  RETURN QUERY SELECT *
               FROM editions
               WHERE newsletter_id = _newsletter_id
                     -- newsletter owner
                     AND (exists(SELECT *
                                 FROM newsletters
                                 WHERE user_id = _user_id)
                          OR
                          -- newsletter writers
                          newsletter_id IN (SELECT newsletter_id
                                            FROM newsletter_writers
                                            WHERE user_id = _user_id)
                          OR
                          -- edition writer
                          id IN (SELECT edition_id
                                 FROM edition_writers
                                 WHERE user_id = _user_id));
END;
$$ LANGUAGE plpgsql STABLE;
