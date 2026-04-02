create table if not exists railway_managers (
  manager_id varchar(64) primary key,
  operator_code varchar(40) not null,
  email varchar(200) not null unique,
  display_name varchar(120) not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create table if not exists trains (
  train_id varchar(64) primary key,
  manager_id varchar(64) not null,
  train_number varchar(32) not null,
  sale_starts_at timestamp not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create table if not exists train_stops (
  stop_id varchar(64) primary key,
  train_id varchar(64) not null,
  station_code varchar(20) not null,
  station_name varchar(120) not null,
  sequence_no integer not null,
  arrival_time timestamp,
  departure_time timestamp
);

create table if not exists train_seat_inventories (
  inventory_id varchar(64) primary key,
  train_id varchar(64) not null,
  seat_class varchar(40) not null,
  total_seats integer not null,
  saleable_seats integer not null,
  status varchar(32) not null
);

create table if not exists train_segment_prices (
  segment_price_id varchar(64) primary key,
  train_id varchar(64) not null,
  from_stop_id varchar(64) not null,
  to_stop_id varchar(64) not null,
  seat_class varchar(40) not null,
  amount decimal(18, 2) not null,
  currency varchar(8) not null
);

create table if not exists train_refund_policy_segments (
  policy_segment_id varchar(64) primary key,
  train_id varchar(64) not null,
  start_offset_minutes_before_departure bigint not null,
  end_offset_minutes_before_departure bigint not null,
  refund_type varchar(32) not null,
  refund_rate decimal(10, 4) not null
);

alter table order_line_items add column if not exists train_id varchar(64);
alter table order_line_items add column if not exists train_from_stop_id varchar(64);
alter table order_line_items add column if not exists train_to_stop_id varchar(64);
alter table order_line_items add column if not exists train_seat_inventory_id varchar(64);
alter table order_line_items add column if not exists seat_class varchar(40);

create index if not exists idx_trains_manager_id on trains(manager_id);
create index if not exists idx_train_stops_train_id on train_stops(train_id, sequence_no);
create index if not exists idx_train_seat_inventories_train_id on train_seat_inventories(train_id);
create index if not exists idx_train_segment_prices_train_id on train_segment_prices(train_id, seat_class);
create index if not exists idx_train_refund_policies_train_id on train_refund_policy_segments(train_id);
