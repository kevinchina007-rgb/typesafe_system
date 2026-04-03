create table if not exists tour_group_chat_settings (
  group_id varchar(64) primary key,
  allow_member_direct_chat boolean not null,
  updated_at timestamp not null,
  updated_by_user_id varchar(64) not null
);

create table if not exists tour_group_conversations (
  conversation_id varchar(64) primary key,
  group_id varchar(64) not null,
  conversation_type varchar(32) not null,
  direct_member_a_user_id varchar(64) null,
  direct_member_b_user_id varchar(64) null,
  created_at timestamp not null
);

create table if not exists tour_group_conversation_participants (
  participant_id varchar(64) primary key,
  conversation_id varchar(64) not null,
  user_id varchar(64) not null,
  role varchar(32) not null,
  joined_at timestamp not null,
  status varchar(32) not null
);

create table if not exists tour_group_messages (
  message_id varchar(64) primary key,
  conversation_id varchar(64) not null,
  sender_user_id varchar(64) not null,
  content varchar(2000) not null,
  status varchar(32) not null,
  created_at timestamp not null
);

create index if not exists idx_tour_group_chat_conversations_group_id
  on tour_group_conversations(group_id);

create index if not exists idx_tour_group_chat_direct_lookup
  on tour_group_conversations(group_id, conversation_type, direct_member_a_user_id, direct_member_b_user_id);

create unique index if not exists idx_tour_group_chat_participants_unique
  on tour_group_conversation_participants(conversation_id, user_id);

create index if not exists idx_tour_group_chat_participants_user_id
  on tour_group_conversation_participants(user_id);

create index if not exists idx_tour_group_messages_conversation_id
  on tour_group_messages(conversation_id, created_at);
