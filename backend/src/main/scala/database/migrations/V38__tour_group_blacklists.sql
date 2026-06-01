alter table tour_groups
  add column if not exists cover_image_url varchar(512);

alter table tour_groups
  add column if not exists tags_json text not null default '[]';

create table if not exists tour_group_blacklists (
  blacklist_id varchar(64) primary key,
  group_id varchar(64) not null,
  user_id varchar(64) not null,
  blacklisted_by_user_id varchar(64) not null,
  reason varchar(500) not null,
  created_at timestamp not null
);

create unique index if not exists idx_tour_group_blacklists_unique
  on tour_group_blacklists(group_id, user_id);

create index if not exists idx_tour_group_blacklists_user_id
  on tour_group_blacklists(user_id);
