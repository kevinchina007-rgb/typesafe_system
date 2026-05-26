alter table airlines add column if not exists logo_asset_path varchar(300);

alter table flights add column if not exists aircraft_model varchar(80) not null default 'A320neo';

create table if not exists flight_airports (
  airport_code varchar(10) primary key,
  city_name varchar(80) not null,
  airport_name varchar(160) not null
);

create index if not exists idx_flight_airports_city_name on flight_airports(city_name);

insert into flight_airports (airport_code, city_name, airport_name) values
  ('PEK', '北京', '首都国际机场'),
  ('PKX', '北京', '大兴国际机场'),
  ('SHA', '上海', '虹桥国际机场'),
  ('PVG', '上海', '浦东国际机场'),
  ('CAN', '广州', '白云国际机场'),
  ('SZX', '深圳', '宝安国际机场'),
  ('CTU', '成都', '双流国际机场'),
  ('TFU', '成都', '天府国际机场'),
  ('CKG', '重庆', '江北国际机场'),
  ('HGH', '杭州', '萧山国际机场'),
  ('NKG', '南京', '禄口国际机场'),
  ('WUH', '武汉', '天河国际机场'),
  ('XIY', '西安', '咸阳国际机场'),
  ('TSN', '天津', '滨海国际机场'),
  ('CGO', '郑州', '新郑国际机场'),
  ('CSX', '长沙', '黄花国际机场'),
  ('TAO', '青岛', '胶东国际机场'),
  ('XMN', '厦门', '高崎国际机场')
on conflict (airport_code) do update set
  city_name = excluded.city_name,
  airport_name = excluded.airport_name;

insert into airlines (airline_id, name, code, status, created_at, logo_asset_path) values
  ('airline-nailong', '卡皮巴拉航空', 'NL', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/NL.svg'),
  ('airline-laoda', '小熊猫航空', 'LD', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/LD.svg'),
  ('airline-mihoyo', '海豚航空', 'MH', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/MH.svg'),
  ('airline-tafei', '长颈鹿航空', 'TF', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/TF.svg'),
  ('airline-genshin', '企鹅航空', 'YS', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/YS.svg'),
  ('airline-wxd', '白鲸航空', 'WX', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/WX.svg'),
  ('airline-zhenxun', '金丝猴航空', 'ZX', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/ZX.svg'),
  ('airline-jntm', '雪豹航空', 'JN', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/JN.svg'),
  ('airline-pangmao', '熊猫航空', 'PM', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/PM.svg'),
  ('airline-niuma', '羚羊航空', 'NM', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', '/images/airlines/NM.svg')
on conflict (airline_id) do update set
  name = excluded.name,
  code = excluded.code,
  status = excluded.status,
  logo_asset_path = excluded.logo_asset_path;

update airlines
set name = case code
  when 'NL' then '奶龙航空'
  when 'LD' then '科比航空'
  when 'TF' then '双子塔航空'
  when 'YS' then '雪豹航空'
  when 'WX' then '星际穿越航空'
  when 'ZX' then '祖国人航空'
  when 'JN' then 'SpaceX航空'
  when 'PM' then '无人驾驶航空'
  when 'NM' then '卡皮巴拉航空'
  when 'MH' then '万户航空'
  when 'MU' then '奶龙航空'
  when '9C' then '科比航空'
  else name
end
where code in ('NL', 'LD', 'TF', 'YS', 'WX', 'ZX', 'JN', 'PM', 'NM', 'MH', 'MU', '9C');

insert into airline_managers (manager_id, airline_id, email, display_name, status, created_at) values
  ('manager-airline-nailong', 'airline-nailong', 'ops@nailong.example', '卡皮巴拉航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-laoda', 'airline-laoda', 'ops@laoda.example', '小熊猫航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-mihoyo', 'airline-mihoyo', 'ops@mihoyo.example', '海豚航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-tafei', 'airline-tafei', 'ops@tafei.example', '长颈鹿航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-genshin', 'airline-genshin', 'ops@genshin.example', '企鹅航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-wxd', 'airline-wxd', 'ops@wxd.example', '白鲸航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-zhenxun', 'airline-zhenxun', 'ops@zhenxun.example', '金丝猴航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-jntm', 'airline-jntm', 'ops@jntm.example', '雪豹航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-pangmao', 'airline-pangmao', 'ops@pangmao.example', '熊猫航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('manager-airline-niuma', 'airline-niuma', 'ops@niuma.example', '羚羊航空运营', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00')
on conflict (manager_id) do update set
  airline_id = excluded.airline_id,
  email = excluded.email,
  display_name = excluded.display_name,
  status = excluded.status;

update airline_managers
set display_name = airlines.name || '运营'
from airlines
where airline_managers.airline_id = airlines.airline_id
  and airlines.code in ('NL', 'LD', 'TF', 'YS', 'WX', 'ZX', 'JN', 'PM', 'NM', 'MH', 'MU', '9C');

insert into manager_credentials (
  credential_id,
  manager_type,
  manager_id,
  login_email,
  password_hash,
  status,
  created_at,
  updated_at,
  password_updated_at
) values
  ('credential-airline-nailong', 'Airline', 'manager-airline-nailong', 'ops@nailong.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LW5haWxvbmctcw==$XxeJxI7PFGrkTrEV0MTH2p5/yRW/itGIyvqlSBkg1ZI=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-laoda', 'Airline', 'manager-airline-laoda', 'ops@laoda.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LWxhb2RhLXNhbA==$RFefh7XGrzycfYoP4Ayx01ejlMY+sXZYpykx02Gc38M=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-mihoyo', 'Airline', 'manager-airline-mihoyo', 'ops@mihoyo.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LW1paG95by1zYQ==$db/iiRQjRSylmpIppXgwTPbtQUFQmsr5T9YnvNYdLek=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-tafei', 'Airline', 'manager-airline-tafei', 'ops@tafei.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LXRhZmVpLXNhbA==$8rO6hS/b0C68QrohTkvnolqNRDvTIAe/xG2D3AC3JbU=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-genshin', 'Airline', 'manager-airline-genshin', 'ops@genshin.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LWdlbnNoaW4tcw==$+g2uAwu2OeBgstAnkd7yyfLeV+7oBbhHhdpgY62Hz7s=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-wxd', 'Airline', 'manager-airline-wxd', 'ops@wxd.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LXd4ZC1zYWx0IQ==$MqM4rR9dtOWollitga5eGVWmG58Q6/LxAKc7CT8VgZ0=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-zhenxun', 'Airline', 'manager-airline-zhenxun', 'ops@zhenxun.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LXpoZW54dW4tcw==$+Q7HDOCGkGWwA1ahfQP7Ij9FRNcCQPWnScVYVNsKflg=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-jntm', 'Airline', 'manager-airline-jntm', 'ops@jntm.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LWpudG0tc2FsdA==$SgeVQI8yJbfpX71bK2wH+9wj0tCpvGKA8LiSUKsVaww=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-pangmao', 'Airline', 'manager-airline-pangmao', 'ops@pangmao.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LXBhbmdtYW8tcw==$lnfn+z9YnVg/CcGVqk/6ZlBcvGlhGpRb/SkOjg8fKNE=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00'),
  ('credential-airline-niuma', 'Airline', 'manager-airline-niuma', 'ops@niuma.example', 'pbkdf2-sha256$210000$ZmxpZ2h0LW5pdW1hLXNhbA==$ntBxnuuI9onrOqVQ24Pozt5VIeTPVqelTN6kLfzF//U=', 'Active', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00', timestamp with time zone '2026-05-18 00:00:00+08:00')
on conflict (manager_type, manager_id) do update set
  login_email = excluded.login_email,
  password_hash = excluded.password_hash,
  status = excluded.status,
  updated_at = excluded.updated_at,
  password_updated_at = excluded.password_updated_at;

with
  cities(city_name, primary_airport, secondary_airport) as (
    values
      ('北京', 'PEK', 'PKX'),
      ('上海', 'SHA', 'PVG'),
      ('广州', 'CAN', null),
      ('深圳', 'SZX', null),
      ('成都', 'CTU', 'TFU'),
      ('重庆', 'CKG', null),
      ('杭州', 'HGH', null),
      ('南京', 'NKG', null),
      ('武汉', 'WUH', null),
      ('西安', 'XIY', null),
      ('天津', 'TSN', null),
      ('郑州', 'CGO', null),
      ('长沙', 'CSX', null),
      ('青岛', 'TAO', null),
      ('厦门', 'XMN', null)
  ),
  airlines_seed(airline_id, airline_code) as (
    values
      ('airline-nailong', 'NL'),
      ('airline-laoda', 'LD'),
      ('airline-mihoyo', 'MH'),
      ('airline-tafei', 'TF'),
      ('airline-genshin', 'YS'),
      ('airline-wxd', 'WX'),
      ('airline-zhenxun', 'ZX'),
      ('airline-jntm', 'JN'),
      ('airline-pangmao', 'PM'),
      ('airline-niuma', 'NM')
  ),
  aircraft_models(model_name) as (
    values ('A320neo'), ('A321neo'), ('B737-800'), ('B787-9'), ('C919')
  ),
  base_rows as (
    select
      origin.city_name as departure_city,
      destination.city_name as arrival_city,
      service_date::date as service_date,
      window_hour,
      ordinal,
      origin.primary_airport as departure_primary_airport,
      origin.secondary_airport as departure_secondary_airport,
      destination.primary_airport as arrival_primary_airport,
      destination.secondary_airport as arrival_secondary_airport,
      origin.city_name || '|' || destination.city_name || '|' || service_date::date || '|' || window_hour || '|' || ordinal as route_key
    from cities origin
    cross join cities destination
    cross join generate_series(date '2026-06-01', date '2026-07-31', interval '1 day') as service_date
    cross join unnest(array[0, 4, 8, 12, 16, 20]) as window_hour
    cross join generate_series(1, 10) as ordinal
    where origin.city_name <> destination.city_name
  ),
  generated as (
    select
      'flight-demo-' || substr(md5(route_key), 1, 24) as flight_id,
      ((abs(hashtext(route_key || '|airline')) % 10) + 1) as airline_rank,
      ((abs(hashtext(route_key || '|model')) % 5) + 1) as model_rank,
      case
        when departure_secondary_airport is not null and mod(abs(hashtext(route_key || '|departure-airport')), 2) = 1 then departure_secondary_airport
        else departure_primary_airport
      end as departure_airport,
      case
        when arrival_secondary_airport is not null and mod(abs(hashtext(route_key || '|arrival-airport')), 2) = 1 then arrival_secondary_airport
        else arrival_primary_airport
      end as arrival_airport,
      service_date,
      window_hour,
      route_key,
      (service_date::timestamp + make_interval(hours => window_hour, mins => mod(abs(hashtext(route_key || '|minute')), 180))) at time zone 'Asia/Shanghai' as departure_time,
      90 + mod(abs(hashtext(route_key || '|duration')), 121) as duration_minutes,
      380 + mod(abs(hashtext(route_key || '|economy-price')), 601) as economy_base_price,
      mod(abs(hashtext(route_key || '|discount-flag')), 10) = 0 as is_discounted,
      70 + mod(abs(hashtext(route_key || '|discount-rate')), 11) as discount_percent
    from base_rows
  ),
  ranked_airlines as (
    select airline_id, airline_code, row_number() over (order by airline_id) as airline_rank
    from airlines_seed
  ),
  ranked_models as (
    select model_name, row_number() over (order by model_name) as model_rank
    from aircraft_models
  ),
  final_rows as (
    select
      generated.flight_id,
      ranked_airlines.airline_id,
      ranked_airlines.airline_code || lpad((1000 + mod(abs(hashtext(generated.route_key || '|flight-number')), 9000))::text, 4, '0') as flight_number,
      ranked_models.model_name as aircraft_model,
      generated.departure_airport,
      generated.arrival_airport,
      generated.departure_time,
      generated.departure_time + make_interval(mins => generated.duration_minutes) as arrival_time,
      generated.economy_base_price,
      generated.is_discounted,
      generated.discount_percent
    from generated
    join ranked_airlines on ranked_airlines.airline_rank = generated.airline_rank
    join ranked_models on ranked_models.model_rank = generated.model_rank
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
  economy_base_price,
  'CNY',
  timestamp with time zone '2026-05-18 00:00:00+08:00'
from final_rows
on conflict (flight_id) do nothing;

with
  cabin_rules(cabin_class, cabin_rank, min_price, max_price, seat_count) as (
    values
      ('ECONOMY', 1, 380, 980, 120),
      ('PREMIUM_ECONOMY', 2, 780, 1680, 36),
      ('BUSINESS', 3, 1680, 3880, 24),
      ('FIRST', 4, 3280, 6880, 8)
  ),
  generated_prices as (
    select
      flights.flight_id,
      cabin_rules.cabin_class,
      cabin_rules.cabin_rank,
      cabin_rules.seat_count,
      cabin_rules.min_price + mod(abs(hashtext(flights.flight_id || '|' || cabin_rules.cabin_class)), cabin_rules.max_price - cabin_rules.min_price + 1) as undiscounted_price,
      mod(abs(hashtext(flights.flight_id || '|discount-flag')), 10) = 0 as is_discounted,
      70 + mod(abs(hashtext(flights.flight_id || '|discount-rate')), 11) as discount_percent
    from flights
    cross join cabin_rules
    where flights.flight_id like 'flight-demo-%'
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
  'cabin-demo-' || cabin_rank || '-' || substr(md5(flight_id || '|' || cabin_class), 1, 24),
  flight_id,
  cabin_class,
  seat_count,
  case
    when is_discounted then round(undiscounted_price * discount_percent / 100.0, 2)
    else undiscounted_price
  end,
  'CNY',
  'Open'
from generated_prices
on conflict (inventory_id) do nothing;
