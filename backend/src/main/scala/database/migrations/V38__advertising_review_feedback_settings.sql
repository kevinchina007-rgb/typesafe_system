alter table feedback_threads add column if not exists manager_actor_id varchar(128);
alter table feedback_threads add column if not exists site_admin_actor_id varchar(128);

create index if not exists idx_feedback_threads_manager_actor
  on feedback_threads(manager_actor_id, manager_type, updated_at desc);

create index if not exists idx_feedback_threads_site_admin_actor
  on feedback_threads(site_admin_actor_id, updated_at desc);

create table if not exists advertisement_delivery_settings (
  placement varchar(128) primary key,
  rotation_interval_seconds integer not null,
  play_order varchar(64) not null,
  start_at timestamp with time zone,
  end_at timestamp with time zone,
  updated_by_manager_id varchar(128),
  updated_at timestamp with time zone not null
);
