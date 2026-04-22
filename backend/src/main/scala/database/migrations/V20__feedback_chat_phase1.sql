create table if not exists feedback_threads (
  thread_id varchar(128) primary key,
  kind varchar(64) not null,
  manager_type varchar(64) not null,
  owner_user_id varchar(128),
  owner_user_display_name varchar(255) not null,
  title varchar(255) not null,
  subtitle varchar(255) not null,
  resource_type varchar(128) not null,
  resource_summary_title varchar(255) not null,
  order_id varchar(128),
  order_item_id varchar(128),
  review_id varchar(128),
  related_thread_id varchar(128),
  unread_by_user integer not null default 0,
  unread_by_manager integer not null default 0,
  unread_by_site_admin integer not null default 0,
  created_at timestamp not null,
  updated_at timestamp not null
);

create index if not exists idx_feedback_threads_owner_user_id on feedback_threads(owner_user_id);
create index if not exists idx_feedback_threads_manager_type on feedback_threads(manager_type);
create index if not exists idx_feedback_threads_review_id on feedback_threads(review_id);
create index if not exists idx_feedback_threads_kind on feedback_threads(kind);

create table if not exists feedback_messages (
  message_id varchar(128) primary key,
  thread_id varchar(128) not null,
  sender_role varchar(64) not null,
  sender_display_name varchar(255) not null,
  body text not null,
  sent_at timestamp not null,
  constraint fk_feedback_messages_thread
    foreign key (thread_id) references feedback_threads(thread_id)
      on delete cascade
);

create index if not exists idx_feedback_messages_thread_id on feedback_messages(thread_id);
create index if not exists idx_feedback_messages_sent_at on feedback_messages(sent_at);
