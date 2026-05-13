alter table advertisements add column if not exists slot_index integer;

create index if not exists idx_advertisements_slot_lookup
  on advertisements(placement, slot_index);
