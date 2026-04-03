alter table user_credentials
  add column if not exists password_updated_at timestamp with time zone;

update user_credentials
set password_updated_at = coalesce(password_updated_at, updated_at, created_at);

alter table user_credentials
  alter column password_updated_at set not null;

alter table manager_credentials
  add column if not exists password_updated_at timestamp with time zone;

update manager_credentials
set password_updated_at = coalesce(password_updated_at, updated_at, created_at);

alter table manager_credentials
  alter column password_updated_at set not null;

alter table auth_sessions
  add column if not exists revoked_at timestamp with time zone;

alter table auth_sessions
  add column if not exists revoke_reason varchar(80);

create index if not exists idx_auth_sessions_actor_status
  on auth_sessions(actor_type, actor_id, status, expires_at desc);
