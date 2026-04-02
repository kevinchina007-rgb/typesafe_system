alter table order_line_items add flight_id varchar(120);
alter table order_line_items add room_type_id varchar(120);
alter table order_line_items add cabin_class varchar(40);
alter table order_line_items add check_in_date date;
alter table order_line_items add check_out_date date;
alter table order_line_items add room_count integer;
alter table order_line_items add traveler_ids_json text;
alter table order_line_items add unit_amount decimal(18, 2);
alter table order_line_items add unit_currency varchar(10);

create index idx_order_line_items_order_kind on order_line_items(order_id, item_kind);
create index idx_order_line_items_flight on order_line_items(flight_id);
create index idx_order_line_items_room_type on order_line_items(room_type_id);

alter table order_payments add created_at timestamp with time zone;
alter table order_payments add metadata_json text;
update order_payments set created_at = authorized_at where created_at is null;
create index idx_order_payments_order_created on order_payments(order_id, created_at);

alter table order_refunds add created_at timestamp with time zone;
alter table order_refunds add metadata_json text;
update order_refunds set created_at = requested_at where created_at is null;
create index idx_order_refunds_order_created on order_refunds(order_id, created_at);

alter table flight_cabin_inventories add version_number integer default 0 not null;
alter table flight_cabin_inventories add updated_at timestamp with time zone;
alter table flight_cabin_inventories add constraint uq_flight_inventory_flight_cabin unique (flight_id, cabin_class);
create index idx_flight_inventory_flight_cabin on flight_cabin_inventories(flight_id, cabin_class);

alter table hotel_room_inventories add version_number integer default 0 not null;
alter table hotel_room_inventories add updated_at timestamp with time zone;
alter table hotel_room_inventories add constraint uq_hotel_room_inventory_room_date unique (room_type_id, inventory_date);
create index idx_hotel_room_inventory_room_date on hotel_room_inventories(room_type_id, inventory_date);
