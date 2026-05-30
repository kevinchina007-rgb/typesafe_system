update railway_managers
set operator_code = 'HH306',
    email = 'ops@hh306.example',
    display_name = '哼哼306'
where manager_id = 'manager-train-hh306';

update trains
set train_number = case train_id
  when 'train-hh306-g1001' then 'H1001'
  when 'train-hh306-g1002' then 'H1002'
  when 'train-hh306-g1003' then 'H1003'
  when 'train-hh306-g1004' then 'H1004'
  when 'train-hh306-g1005' then 'H1005'
  else train_number
end
where train_id in (
  'train-hh306-g1001',
  'train-hh306-g1002',
  'train-hh306-g1003',
  'train-hh306-g1004',
  'train-hh306-g1005'
);

update train_stops
set station_name = case station_code
  when 'BJS' then '北京南'
  when 'BJX' then '北京西'
  when 'TJS' then '天津南'
  when 'JNW' then '济南西'
  when 'NJS' then '南京南'
  when 'SHH' then '上海虹桥'
  when 'SHN' then '上海南'
  when 'SZB' then '苏州北'
  when 'HZD' then '杭州东'
  when 'NGB' then '宁波'
  when 'WZS' then '温州南'
  when 'GZQ' then '广州南'
  when 'SZN' then '深圳北'
  when 'XMN' then '厦门北'
  when 'CDD' then '成都东'
  when 'CQB' then '重庆北'
  when 'WUH' then '武汉'
  when 'CSN' then '长沙南'
  when 'ZZD' then '郑州东'
  when 'XAB' then '西安北'
  when 'HFN' then '合肥南'
  when 'QDB' then '青岛北'
  when 'TYN' then '太原南'
  when 'FZN' then '福州南'
  when 'NCX' then '南昌西'
  else station_name
end
where station_code in (
  'BJS', 'BJX', 'TJS', 'JNW', 'NJS', 'SHH', 'SHN', 'SZB', 'HZD', 'NGB', 'WZS',
  'GZQ', 'SZN', 'XMN', 'CDD', 'CQB', 'WUH', 'CSN', 'ZZD', 'XAB', 'HFN', 'QDB',
  'TYN', 'FZN', 'NCX'
);

update train_seats
set seat_label = lpad(carriage_no::text, 2, '0') || '车 ' || seat_no
where seat_label is distinct from lpad(carriage_no::text, 2, '0') || '车 ' || seat_no;
