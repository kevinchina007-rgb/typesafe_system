create table if not exists inventory_reservations (
  reservation_id varchar(120) primary key,
  resource_type varchar(64) not null,
  resource_id varchar(120) not null,
  order_id varchar(120) not null,
  order_item_id varchar(120) not null,
  quantity integer not null,
  status varchar(32) not null,
  reserved_at timestamp with time zone not null,
  expires_at timestamp with time zone not null,
  confirmed_at timestamp with time zone,
  released_at timestamp with time zone,
  constraint fk_inventory_reservations_order foreign key (order_id) references orders(order_id)
);

create index if not exists idx_inventory_reservations_resource on inventory_reservations(resource_type, resource_id, status, expires_at);
create index if not exists idx_inventory_reservations_order on inventory_reservations(order_id);
create index if not exists idx_inventory_reservations_order_item on inventory_reservations(order_item_id);
create index if not exists idx_inventory_reservations_status_expiry on inventory_reservations(status, expires_at);
