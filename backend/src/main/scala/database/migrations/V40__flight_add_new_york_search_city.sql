insert into flight_airports (airport_code, city_name, airport_name) values
  ('JFK', U&'\7EBD\7EA6', U&'\7EA6\7FF0\00B7F.\80AF\5C3C\8FEA\56FD\9645\673A\573A')
on conflict (airport_code) do update set
  city_name = excluded.city_name,
  airport_name = excluded.airport_name;

with route_rows as (
  select
    airport_code as domestic_airport,
    row_number() over (order by airport_code) as rn
  from flight_airports
  where city_name in (
    U&'\5317\4EAC',
    U&'\4E0A\6D77',
    U&'\5E7F\5DDE',
    U&'\6DF1\5733',
    U&'\6210\90FD',
    U&'\676D\5DDE'
  )
),
generated as (
  select
    'flight-demo-ny-' || substr(md5(domestic_airport || '-' || direction || '-' || service_date::text || '-' || slot_index::text), 1, 22) as flight_id,
    case when mod(rn, 2) = 0 then 'airline-laoda' else 'airline-nailong' end as airline_id,
    case when direction = 'outbound' then domestic_airport else 'JFK' end as departure_airport,
    case when direction = 'outbound' then 'JFK' else domestic_airport end as arrival_airport,
    ((service_date::timestamp + make_interval(hours => case when direction = 'outbound' then 9 else 8 end + slot_index * 3)) at time zone 'Asia/Shanghai') as departure_time,
    (780 + mod(rn * 37 + slot_index * 19, 121))::int as duration_minutes,
    4200 + mod(rn * 211 + slot_index * 97, 1600) as base_price_amount,
    row_number() over (order by domestic_airport, direction, service_date, slot_index) as seq_no
  from route_rows
  cross join (values ('outbound'), ('inbound')) as directions(direction)
  cross join generate_series(date '2026-06-06', date '2026-07-31', interval '7 day') as service_date
  cross join generate_series(0, 1) as slot_index
),
final_rows as (
  select
    flight_id,
    airline_id,
    upper(substr(replace(airline_id, 'airline-', ''), 1, 2)) || lpad((2000 + seq_no)::text, 4, '0') as flight_number,
    'B787-9' as aircraft_model,
    departure_airport,
    arrival_airport,
    departure_time,
    departure_time + make_interval(mins => duration_minutes::int) as arrival_time,
    base_price_amount
  from generated
)
insert into flights (
  flight_id,
  airline_id,
  flight_number,
  aircraft_model,
  departure_airport,
  arrival_airport,
  departure_time,
  arrival_time,
  status,
  base_price_amount,
  base_price_currency,
  created_at
)
select
  flight_id,
  airline_id,
  flight_number,
  aircraft_model,
  departure_airport,
  arrival_airport,
  departure_time,
  arrival_time,
  'OpenForBooking',
  base_price_amount,
  'CNY',
  timestamp with time zone '2026-06-06 00:00:00+08:00'
from final_rows
on conflict (flight_id) do nothing;

with cabin_rules(cabin_class, cabin_rank, seat_count, base_markup) as (
  values
    ('ECONOMY', 1, 120, 0),
    ('PREMIUM_ECONOMY', 2, 36, 1800),
    ('BUSINESS', 3, 24, 5200),
    ('FIRST', 4, 8, 9800)
)
insert into flight_cabin_inventories (
  inventory_id,
  flight_id,
  cabin_class,
  available_seats,
  unit_price_amount,
  unit_price_currency,
  status
)
select
  'cabin-demo-ny-' || cabin_rank || '-' || substr(md5(flights.flight_id || '|' || cabin_class), 1, 22),
  flights.flight_id,
  cabin_class,
  seat_count,
  flights.base_price_amount + base_markup,
  'CNY',
  'Open'
from flights
cross join cabin_rules
where flights.flight_id like 'flight-demo-ny-%'
on conflict (inventory_id) do nothing;
