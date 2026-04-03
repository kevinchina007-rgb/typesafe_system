create index if not exists idx_flights_route_departure_time
  on flights(departure_airport, arrival_airport, departure_time desc);

create index if not exists idx_flights_flight_number
  on flights(flight_number);

create index if not exists idx_hotels_location
  on hotels(location);

create index if not exists idx_hotels_name
  on hotels(name);

create index if not exists idx_train_stops_station_code
  on train_stops(station_code);

create index if not exists idx_train_stops_station_name
  on train_stops(station_name);

create index if not exists idx_attractions_city
  on attractions(city);

create index if not exists idx_attractions_name
  on attractions(name);

create index if not exists idx_blog_posts_title
  on blog_posts(title);
