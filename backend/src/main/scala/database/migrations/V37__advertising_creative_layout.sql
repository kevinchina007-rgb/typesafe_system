alter table advertisements add column if not exists advertisement_kind varchar(64) not null default 'ResourcePromotion';
alter table advertisements add column if not exists creative_json text;
alter table advertisements add column if not exists creative_width integer not null default 960;
alter table advertisements add column if not exists creative_height integer not null default 240;

create index if not exists idx_advertisements_kind on advertisements(advertisement_kind);
