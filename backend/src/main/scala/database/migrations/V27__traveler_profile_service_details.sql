alter table traveler_profiles add column if not exists gender varchar(40) not null default 'unspecified';
alter table traveler_profiles add column if not exists nationality varchar(80) not null default 'China';
alter table traveler_profiles add column if not exists document_expiry_date date;
alter table traveler_profiles add column if not exists email varchar(320);
alter table traveler_profiles add column if not exists quiet_seat_preferred boolean not null default false;
alter table traveler_profiles add column if not exists assistance_type varchar(80) not null default 'none';
alter table traveler_profiles add column if not exists special_requirement_note text;
alter table traveler_profiles add column if not exists has_large_luggage boolean not null default false;
alter table traveler_profiles add column if not exists luggage_note text;
