create table if not exists airline_managers (
  manager_id varchar(64) primary key,
  airline_id varchar(64) not null,
  email varchar(255) not null,
  display_name varchar(255) not null,
  status varchar(64) not null,
  created_at timestamp with time zone not null,
  constraint fk_airline_managers_airline foreign key (airline_id) references airlines(airline_id)
);

create unique index if not exists idx_airline_managers_email on airline_managers(email);
create index if not exists idx_airline_managers_airline_id on airline_managers(airline_id);

create table if not exists hotel_managers (
  manager_id varchar(64) primary key,
  hotel_id varchar(64) not null,
  email varchar(255) not null,
  display_name varchar(255) not null,
  status varchar(64) not null,
  created_at timestamp with time zone not null,
  constraint fk_hotel_managers_hotel foreign key (hotel_id) references hotels(hotel_id)
);

create unique index if not exists idx_hotel_managers_email on hotel_managers(email);
create index if not exists idx_hotel_managers_hotel_id on hotel_managers(hotel_id);

alter table order_line_items add column if not exists supplier_review_status varchar(64) not null default 'NotSubmitted';
alter table order_line_items add column if not exists review_decision varchar(64);
alter table order_line_items add column if not exists review_reason varchar(1000);
alter table order_line_items add column if not exists reviewed_at timestamp with time zone;
alter table order_line_items add column if not exists reviewed_by_manager_id varchar(64);

create index if not exists idx_order_line_items_supplier_review_status on order_line_items(supplier_review_status);
