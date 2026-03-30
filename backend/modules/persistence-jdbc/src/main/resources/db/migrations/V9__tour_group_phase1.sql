create table if not exists tour_groups (
  group_id varchar(64) primary key,
  organizer_user_id varchar(64) not null,
  title varchar(160) not null,
  description varchar(500) not null,
  destination varchar(160) not null,
  start_date date not null,
  end_date date not null,
  capacity integer not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create table if not exists tour_group_memberships (
  membership_id varchar(64) primary key,
  group_id varchar(64) not null,
  user_id varchar(64) not null,
  joined_at timestamp not null,
  status varchar(32) not null
);

create table if not exists tour_group_membership_travelers (
  membership_traveler_id varchar(64) primary key,
  membership_id varchar(64) not null,
  traveler_id varchar(64) not null,
  joined_at timestamp not null,
  status varchar(32) not null
);

create table if not exists group_plan_items (
  plan_item_id varchar(64) primary key,
  group_id varchar(64) not null,
  item_type varchar(32) not null,
  title varchar(160) not null,
  description varchar(500) not null,
  scheduled_at timestamp not null,
  ends_at timestamp null,
  sequence_no integer not null,
  status varchar(32) not null
);

create table if not exists group_plan_options (
  option_id varchar(64) primary key,
  plan_item_id varchar(64) not null,
  resource_type varchar(32) not null,
  resource_id varchar(64) not null,
  resource_variant_code varchar(64) null,
  resource_context varchar(200) null,
  label varchar(160) not null,
  description varchar(500) not null,
  default_quantity integer not null,
  status varchar(32) not null
);

create table if not exists group_plan_selections (
  selection_id varchar(64) primary key,
  group_id varchar(64) not null,
  plan_item_id varchar(64) not null,
  option_id varchar(64) not null,
  membership_id varchar(64) not null,
  quantity integer not null,
  status varchar(32) not null,
  created_at timestamp not null,
  confirmed_at timestamp null,
  reviewed_by_organizer_user_id varchar(64) null,
  review_note varchar(500) null
);

create table if not exists group_plan_selection_travelers (
  selection_traveler_id varchar(64) primary key,
  selection_id varchar(64) not null,
  traveler_id varchar(64) not null
);

create table if not exists group_selection_order_links (
  link_id varchar(64) primary key,
  selection_id varchar(64) not null,
  order_id varchar(64) not null,
  created_at timestamp not null
);

create index if not exists idx_tour_group_memberships_group_id on tour_group_memberships(group_id);
create index if not exists idx_tour_group_memberships_user_id on tour_group_memberships(user_id);
create index if not exists idx_tour_group_membership_travelers_membership_id on tour_group_membership_travelers(membership_id);
create index if not exists idx_group_plan_items_group_id on group_plan_items(group_id);
create unique index if not exists idx_group_plan_items_group_sequence on group_plan_items(group_id, sequence_no);
create index if not exists idx_group_plan_options_plan_item_id on group_plan_options(plan_item_id);
create index if not exists idx_group_plan_selections_group_id on group_plan_selections(group_id);
create index if not exists idx_group_plan_selections_membership_id on group_plan_selections(membership_id);
create index if not exists idx_group_plan_selection_travelers_selection_id on group_plan_selection_travelers(selection_id);
create unique index if not exists idx_group_plan_selection_travelers_unique on group_plan_selection_travelers(selection_id, traveler_id);
create unique index if not exists idx_group_selection_order_links_selection_id on group_selection_order_links(selection_id);
