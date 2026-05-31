create table if not exists site_admin_managers (
  manager_id varchar(80) primary key,
  email varchar(320) not null unique,
  display_name varchar(160) not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null
);

insert into site_admin_managers (manager_id, email, display_name, status, created_at)
select c.manager_id, c.login_email, split_part(c.login_email, '@', 1), c.status, c.created_at
from manager_credentials c
where c.manager_type = 'SiteAdmin'
on conflict (manager_id) do nothing;
