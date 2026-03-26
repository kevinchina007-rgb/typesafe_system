create table if not exists users (
  user_id varchar(120) primary key,
  email varchar(320) not null unique,
  phone varchar(80) not null,
  nickname varchar(160) not null,
  avatar_url varchar(300),
  membership_level varchar(40) not null,
  points bigint not null,
  status varchar(40) not null,
  default_traveler_id varchar(120),
  created_at timestamp with time zone not null
);

create table if not exists traveler_profiles (
  traveler_id varchar(120) primary key,
  owner_user_id varchar(120) not null,
  full_name varchar(160) not null,
  birth_date date not null,
  document_type varchar(80) not null,
  document_number varchar(120) not null,
  phone varchar(80) not null,
  traveler_type varchar(80) not null,
  status varchar(40) not null,
  is_default boolean not null,
  preferences_json clob not null,
  emergency_contact_json clob,
  identity_documents_json clob not null,
  loyalty_memberships_json clob not null,
  constraint fk_traveler_owner_user foreign key (owner_user_id) references users(user_id),
  constraint uq_traveler_document_identity unique (document_type, document_number)
);

create table if not exists orders (
  order_id varchar(120) primary key,
  buyer_user_id varchar(120) not null,
  order_type varchar(40) not null,
  status varchar(40) not null,
  currency varchar(10) not null,
  total_price_amount decimal(18, 2) not null,
  remaining_refundable_amount decimal(18, 2) not null,
  created_at timestamp with time zone not null,
  paid_at timestamp with time zone,
  confirmed_at timestamp with time zone,
  completed_at timestamp with time zone,
  cancelled_at timestamp with time zone,
  constraint fk_order_buyer_user foreign key (buyer_user_id) references users(user_id)
);

create table if not exists order_line_items (
  order_item_id varchar(120) primary key,
  order_id varchar(120) not null,
  item_kind varchar(40) not null,
  item_status varchar(40) not null,
  booked_amount decimal(18, 2) not null,
  booked_currency varchar(10) not null,
  snapshot_json clob not null,
  sort_index integer not null,
  constraint fk_order_line_item_order foreign key (order_id) references orders(order_id)
);

create table if not exists order_payments (
  payment_id varchar(120) primary key,
  order_id varchar(120) not null,
  payment_amount decimal(18, 2) not null,
  payment_currency varchar(10) not null,
  payment_method varchar(40) not null,
  payment_status varchar(40) not null,
  authorized_at timestamp with time zone not null,
  captured_at timestamp with time zone,
  constraint fk_order_payment_order foreign key (order_id) references orders(order_id)
);

create table if not exists order_refunds (
  refund_id varchar(120) primary key,
  order_id varchar(120) not null,
  refund_amount decimal(18, 2) not null,
  refund_currency varchar(10) not null,
  refund_reason varchar(400) not null,
  refund_status varchar(40) not null,
  requested_at timestamp with time zone not null,
  approved_at timestamp with time zone,
  settled_at timestamp with time zone,
  constraint fk_order_refund_order foreign key (order_id) references orders(order_id)
);

create table if not exists airlines (
  airline_id varchar(120) primary key,
  name varchar(160) not null,
  code varchar(10) not null unique,
  status varchar(40) not null,
  created_at timestamp with time zone not null
);

create table if not exists flights (
  flight_id varchar(120) primary key,
  airline_id varchar(120) not null,
  flight_number varchar(40) not null,
  departure_airport varchar(10) not null,
  arrival_airport varchar(10) not null,
  departure_time timestamp with time zone not null,
  arrival_time timestamp with time zone not null,
  status varchar(40) not null,
  base_price_amount decimal(18, 2) not null,
  base_price_currency varchar(10) not null,
  created_at timestamp with time zone not null,
  constraint fk_flight_airline foreign key (airline_id) references airlines(airline_id)
);

create table if not exists flight_cabin_inventories (
  inventory_id varchar(120) primary key,
  flight_id varchar(120) not null,
  cabin_class varchar(40) not null,
  available_seats integer not null,
  unit_price_amount decimal(18, 2) not null,
  unit_price_currency varchar(10) not null,
  status varchar(40) not null,
  constraint fk_flight_inventory_flight foreign key (flight_id) references flights(flight_id)
);

create table if not exists hotels (
  hotel_id varchar(120) primary key,
  name varchar(160) not null,
  location varchar(160) not null,
  status varchar(40) not null,
  created_at timestamp with time zone not null
);

create table if not exists hotel_room_types (
  room_type_id varchar(120) primary key,
  hotel_id varchar(120) not null,
  name varchar(160) not null,
  capacity integer not null,
  bed_type varchar(40) not null,
  base_price_amount decimal(18, 2) not null,
  base_price_currency varchar(10) not null,
  status varchar(40) not null,
  constraint fk_hotel_room_type_hotel foreign key (hotel_id) references hotels(hotel_id)
);

create table if not exists hotel_room_inventories (
  inventory_id varchar(120) primary key,
  room_type_id varchar(120) not null,
  inventory_date date not null,
  available_rooms integer not null,
  unit_price_amount decimal(18, 2) not null,
  unit_price_currency varchar(10) not null,
  status varchar(40) not null,
  constraint fk_hotel_room_inventory_room_type foreign key (room_type_id) references hotel_room_types(room_type_id)
);
