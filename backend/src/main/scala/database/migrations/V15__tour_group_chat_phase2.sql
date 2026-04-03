alter table tour_group_conversations
  add column if not exists status varchar(32) not null default 'Active';

alter table tour_group_conversations
  add column if not exists updated_at timestamp not null default now();

alter table tour_group_conversation_participants
  add column if not exists last_read_at timestamp null;

alter table tour_group_conversation_participants
  add column if not exists last_read_message_id varchar(64) null;

alter table tour_group_conversation_participants
  add column if not exists muted_at timestamp null;

alter table tour_group_conversation_participants
  add column if not exists archived_at timestamp null;

alter table tour_group_messages
  add column if not exists message_type varchar(32) not null default 'Text';

alter table tour_group_messages
  add column if not exists reply_to_message_id varchar(64) null;

alter table tour_group_messages
  add column if not exists forwarded_from_message_id varchar(64) null;

alter table tour_group_messages
  add column if not exists updated_at timestamp not null default now();

alter table tour_group_messages
  add column if not exists deleted_at timestamp null;

alter table tour_group_messages
  add column if not exists recalled_at timestamp null;

create table if not exists tour_group_message_attachments (
  attachment_id varchar(64) primary key,
  message_id varchar(64) not null,
  attachment_type varchar(32) not null,
  public_url varchar(512) not null,
  storage_path varchar(512) not null,
  original_file_name varchar(255) not null,
  mime_type varchar(160) not null,
  file_size bigint not null,
  sort_order integer not null,
  created_at timestamp not null
);

create table if not exists tour_group_message_reactions (
  reaction_id varchar(64) primary key,
  message_id varchar(64) not null,
  user_id varchar(64) not null,
  reaction_type varchar(32) not null,
  created_at timestamp not null
);

create unique index if not exists idx_tour_group_message_reactions_unique
  on tour_group_message_reactions(message_id, user_id, reaction_type);

create index if not exists idx_tour_group_conversations_updated_at
  on tour_group_conversations(group_id, updated_at desc);

create index if not exists idx_tour_group_conversation_participants_read_state
  on tour_group_conversation_participants(user_id, conversation_id, last_read_at);

create index if not exists idx_tour_group_message_attachments_message_id
  on tour_group_message_attachments(message_id, sort_order);

create index if not exists idx_tour_group_messages_search
  on tour_group_messages(conversation_id, status, created_at desc);
