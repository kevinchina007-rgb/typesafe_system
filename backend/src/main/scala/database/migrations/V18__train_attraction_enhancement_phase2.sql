create table if not exists train_seats (
  seat_id varchar primary key,
  train_id varchar not null references trains(train_id) on delete cascade,
  inventory_id varchar not null references train_seat_inventories(inventory_id) on delete cascade,
  seat_class varchar not null,
  carriage_no integer not null,
  row_no integer not null,
  seat_code varchar not null,
  seat_no varchar not null,
  seat_label varchar not null,
  seat_position_type varchar not null,
  status varchar not null
);

create index if not exists idx_train_seats_train_inventory on train_seats(train_id, inventory_id);
create index if not exists idx_train_seats_train_class on train_seats(train_id, seat_class, carriage_no, row_no);

create table if not exists train_seat_allocations (
  allocation_id varchar primary key,
  seat_id varchar not null references train_seats(seat_id) on delete cascade,
  train_id varchar not null references trains(train_id) on delete cascade,
  order_id varchar not null references orders(order_id) on delete cascade,
  order_item_id varchar not null references order_line_items(order_item_id) on delete cascade,
  traveler_id varchar not null,
  from_stop_sequence_no integer not null,
  to_stop_sequence_no integer not null,
  carriage_no integer not null,
  seat_no varchar not null,
  seat_label varchar not null,
  seat_position_type varchar not null,
  created_at timestamp with time zone not null
);

create index if not exists idx_train_seat_allocations_train on train_seat_allocations(train_id, seat_id, from_stop_sequence_no, to_stop_sequence_no);
create index if not exists idx_train_seat_allocations_order on train_seat_allocations(order_id, order_item_id);

create table if not exists attraction_ticket_sessions (
  session_id varchar primary key,
  ticket_type_id varchar not null references ticket_types(ticket_type_id) on delete cascade,
  session_name varchar not null,
  use_date date not null,
  starts_at timestamp with time zone not null,
  ends_at timestamp with time zone not null,
  capacity integer not null,
  status varchar not null,
  created_at timestamp with time zone not null
);

create index if not exists idx_attraction_ticket_sessions_ticket_date on attraction_ticket_sessions(ticket_type_id, use_date, starts_at);
