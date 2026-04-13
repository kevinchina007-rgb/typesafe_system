create table if not exists uploaded_binary_assets (
  asset_id varchar(120) primary key,
  owner_user_id varchar(120),
  asset_category varchar(80) not null,
  original_file_name varchar(255) not null,
  file_extension varchar(32) not null,
  mime_type varchar(160) not null,
  file_size bigint not null,
  binary_content bytea not null,
  created_at timestamp with time zone not null
);

create index if not exists idx_uploaded_binary_assets_owner_created_at
  on uploaded_binary_assets(owner_user_id, created_at desc);
