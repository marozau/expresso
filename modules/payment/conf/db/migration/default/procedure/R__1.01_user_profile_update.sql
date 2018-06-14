create or replace function user_profile_update(
  _user_id       BIGINT,
  _status        user_status,
  _first_name    TEXT,
  _last_name     TEXT,
  _date_of_birth DATE,
  _country       CHAR(2),
  _city          TEXT,
  _postcode      TEXT
)
  returns setof user_profiles as $$
begin

  return query
  insert into user_profiles (user_id, status, first_name, last_name, date_of_birth, country, city, postcode)
  values (_user_id, _status, _first_name, _last_name, _date_of_birth, _country, _city, _postcode)
  on conflict (user_id)
    do update set
      status        = coalesce(_status, user_profiles.status),
      first_name    = coalesce(_first_name, user_profiles.first_name),
      last_name     = coalesce(_last_name, user_profiles.last_name),
      date_of_birth = coalesce(_date_of_birth, user_profiles.date_of_birth),
      country       = coalesce(_country, user_profiles.country),
      city          = coalesce(_city, user_profiles.city),
      postcode      = coalesce(_postcode, user_profiles.postcode)
  returning *;
end;
$$
language plpgsql;