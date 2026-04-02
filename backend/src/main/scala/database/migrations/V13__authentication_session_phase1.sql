create table if not exists user_credentials (
  credential_id varchar(100) primary key,
  user_id varchar(100) not null unique,
  login_email varchar(320) not null unique,
  password_hash text not null,
  status varchar(40) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table if not exists manager_credentials (
  credential_id varchar(100) primary key,
  manager_type varchar(40) not null,
  manager_id varchar(100) not null,
  login_email varchar(320) not null,
  password_hash text not null,
  status varchar(40) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  unique (manager_type, manager_id),
  unique (manager_type, login_email)
);

create table if not exists auth_sessions (
  session_id varchar(100) primary key,
  actor_type varchar(40) not null,
  actor_id varchar(100) not null,
  manager_type varchar(40),
  created_at timestamp with time zone not null,
  last_seen_at timestamp with time zone not null,
  expires_at timestamp with time zone not null,
  status varchar(40) not null
);

create index if not exists idx_auth_sessions_actor on auth_sessions(actor_type, actor_id);
create index if not exists idx_auth_sessions_expires_at on auth_sessions(expires_at);
