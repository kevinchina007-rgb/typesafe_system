begin;

delete from train_seat_allocations
where train_id in (
  select train_id
  from trains
  where train_number like 'G%'
);

delete from train_seats
where train_id in (
  select train_id
  from trains
  where train_number like 'G%'
);

delete from train_segment_prices
where train_id in (
  select train_id
  from trains
  where train_number like 'G%'
);

delete from train_refund_policy_segments
where train_id in (
  select train_id
  from trains
  where train_number like 'G%'
);

delete from train_seat_inventories
where train_id in (
  select train_id
  from trains
  where train_number like 'G%'
);

delete from train_stops
where train_id in (
  select train_id
  from trains
  where train_number like 'G%'
);

delete from trains
where train_number like 'G%';

commit;
