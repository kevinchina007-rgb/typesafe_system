create table if not exists blog_profile_settings (
  user_id varchar(64) primary key references users(user_id) on delete cascade,
  hide_relations boolean not null default false,
  updated_at timestamptz not null default now()
);
