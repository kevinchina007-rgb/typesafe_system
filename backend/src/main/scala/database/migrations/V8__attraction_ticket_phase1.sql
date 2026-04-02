create table if not exists attraction_managers (
  manager_id varchar(64) primary key,
  email varchar(200) not null unique,
  display_name varchar(120) not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create table if not exists attractions (
  attraction_id varchar(64) primary key,
  manager_id varchar(64) not null,
  name varchar(160) not null,
  city varchar(120) not null,
  location varchar(160) not null,
  description varchar(500) not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create table if not exists ticket_types (
  ticket_type_id varchar(64) primary key,
  attraction_id varchar(64) not null,
  name varchar(120) not null,
  description varchar(240) not null,
  price_amount decimal(18, 2) not null,
  price_currency varchar(8) not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create table if not exists ticket_type_rules (
  rule_id varchar(64) primary key,
  ticket_type_id varchar(64) not null,
  rule_type varchar(64) not null,
  rule_config_json text not null,
  created_at timestamp not null
);

alter table order_line_items add column if not exists attraction_id varchar(64);
alter table order_line_items add column if not exists ticket_type_id varchar(64);
alter table order_line_items add column if not exists use_date date;

create index if not exists idx_attractions_manager_id on attractions(manager_id);
create index if not exists idx_ticket_types_attraction_id on ticket_types(attraction_id);
create index if not exists idx_ticket_type_rules_ticket_type_id on ticket_type_rules(ticket_type_id);
