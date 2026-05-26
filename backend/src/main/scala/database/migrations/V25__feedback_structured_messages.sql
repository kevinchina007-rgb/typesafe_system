alter table feedback_messages add column if not exists message_type varchar(64) not null default 'text';
alter table feedback_messages add column if not exists payload_json text;
alter table feedback_messages add column if not exists is_read boolean not null default false;

create index if not exists idx_feedback_messages_message_type on feedback_messages(message_type);
