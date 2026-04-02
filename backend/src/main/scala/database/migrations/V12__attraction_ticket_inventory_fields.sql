alter table ticket_types add column if not exists available_from_date date;
alter table ticket_types add column if not exists available_to_date date;
alter table ticket_types add column if not exists total_quantity integer;
alter table ticket_types add column if not exists valid_weekdays varchar(128);

update ticket_types
set
  available_from_date = coalesce(available_from_date, cast(created_at as date)),
  available_to_date = coalesce(available_to_date, cast(created_at as date)),
  total_quantity = coalesce(total_quantity, 9999),
  valid_weekdays = coalesce(valid_weekdays, 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY');
