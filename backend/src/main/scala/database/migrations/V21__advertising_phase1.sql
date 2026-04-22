create table if not exists advertisements (
  advertisement_id varchar(128) primary key,
  owner_manager_id varchar(128) not null,
  owner_type varchar(64) not null,
  owner_display_name varchar(255) not null,
  target_resource_type varchar(64) not null,
  target_resource_id varchar(128) not null,
  resource_summary_title varchar(255) not null,
  landing_target varchar(512) not null,
  placement varchar(128) not null,
  audience varchar(64) not null,
  title varchar(255) not null,
  subtitle varchar(255) not null,
  description text not null,
  image_url varchar(1024),
  cta_label varchar(128) not null,
  review_status varchar(64) not null,
  delivery_status varchar(64) not null,
  priority integer not null,
  start_at timestamptz not null,
  end_at timestamptz not null,
  rejection_note text,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create index if not exists idx_advertisements_owner on advertisements(owner_manager_id, owner_type);
create index if not exists idx_advertisements_review_status on advertisements(review_status);
create index if not exists idx_advertisements_delivery_lookup on advertisements(placement, review_status, delivery_status, start_at, end_at);

create table if not exists advertisement_reviews (
  review_id varchar(128) primary key,
  advertisement_id varchar(128) not null references advertisements(advertisement_id) on delete cascade,
  reviewer_manager_id varchar(128) not null,
  decision varchar(64) not null,
  review_note text,
  reviewed_at timestamptz not null
);

create index if not exists idx_advertisement_reviews_advertisement_id on advertisement_reviews(advertisement_id, reviewed_at desc);
