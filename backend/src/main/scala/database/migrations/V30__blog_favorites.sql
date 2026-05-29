create table if not exists blog_favorites (
  favorite_id varchar(80) primary key,
  post_id varchar(120) not null,
  user_id varchar(80) not null,
  created_at timestamp not null,
  constraint fk_blog_favorites_post foreign key (post_id) references blog_posts(post_id),
  constraint fk_blog_favorites_user foreign key (user_id) references users(user_id)
);

create unique index if not exists uq_blog_favorites_post_user
  on blog_favorites(post_id, user_id);

