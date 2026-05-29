alter table blog_posts
  add column if not exists cover_text text;

alter table blog_posts
  add column if not exists travel_city varchar(120);

alter table blog_comments
  add column if not exists parent_comment_id varchar(80);

alter table blog_comments
  add column if not exists reply_to_user_id varchar(80);

create table if not exists blog_post_tags (
  post_id varchar(80) not null,
  tag_type varchar(80) not null,
  tag_value varchar(120) not null,
  constraint fk_blog_post_tags_post foreign key (post_id) references blog_posts(post_id)
);

create unique index if not exists uq_blog_post_tags_post_type_value
  on blog_post_tags(post_id, tag_type, tag_value);

create table if not exists blog_comment_likes (
  like_id varchar(80) primary key,
  comment_id varchar(80) not null,
  user_id varchar(80) not null,
  created_at timestamp not null,
  constraint fk_blog_comment_likes_comment foreign key (comment_id) references blog_comments(comment_id),
  constraint fk_blog_comment_likes_user foreign key (user_id) references users(user_id)
);

create unique index if not exists uq_blog_comment_likes_comment_user
  on blog_comment_likes(comment_id, user_id);

create table if not exists blog_user_follows (
  follow_id varchar(80) primary key,
  follower_user_id varchar(80) not null,
  target_user_id varchar(80) not null,
  created_at timestamp not null,
  constraint fk_blog_user_follows_follower foreign key (follower_user_id) references users(user_id),
  constraint fk_blog_user_follows_target foreign key (target_user_id) references users(user_id)
);

create unique index if not exists uq_blog_user_follows_pair
  on blog_user_follows(follower_user_id, target_user_id);

create table if not exists blog_user_blocks (
  block_id varchar(80) primary key,
  blocker_user_id varchar(80) not null,
  target_user_id varchar(80) not null,
  created_at timestamp not null,
  constraint fk_blog_user_blocks_blocker foreign key (blocker_user_id) references users(user_id),
  constraint fk_blog_user_blocks_target foreign key (target_user_id) references users(user_id)
);

create unique index if not exists uq_blog_user_blocks_pair
  on blog_user_blocks(blocker_user_id, target_user_id);

create table if not exists blog_notifications (
  notification_id varchar(80) primary key,
  receiver_user_id varchar(80) not null,
  actor_user_id varchar(80),
  notification_type varchar(80) not null,
  post_id varchar(80),
  comment_id varchar(80),
  content text not null,
  is_read boolean not null default false,
  created_at timestamp not null,
  constraint fk_blog_notifications_receiver foreign key (receiver_user_id) references users(user_id),
  constraint fk_blog_notifications_actor foreign key (actor_user_id) references users(user_id)
);

delete from blog_notifications;
delete from blog_user_blocks;
delete from blog_user_follows;
delete from blog_comment_likes;
delete from blog_post_tags;
delete from blog_likes;
delete from blog_comments;
delete from blog_post_images;
delete from blog_posts;
