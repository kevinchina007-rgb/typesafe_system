alter table inventory_reservations add column if not exists check_in_date date;
alter table inventory_reservations add column if not exists check_out_date date;

create index if not exists idx_inventory_reservations_stay_range
  on inventory_reservations(resource_type, resource_id, check_in_date, check_out_date);
