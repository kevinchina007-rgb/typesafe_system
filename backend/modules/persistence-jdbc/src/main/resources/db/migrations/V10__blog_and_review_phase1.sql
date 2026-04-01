create table if not exists blog_posts (
  post_id varchar(120) primary key,
  author_user_id varchar(120) not null,
  title varchar(200) not null,
  summary text not null,
  content text not null,
  status varchar(40) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  published_at timestamp with time zone,
  constraint fk_blog_posts_author foreign key (author_user_id) references users(user_id)
);

create index if not exists idx_blog_posts_status_published_at
  on blog_posts(status, published_at desc);

create index if not exists idx_blog_posts_author_created_at
  on blog_posts(author_user_id, created_at desc);

create table if not exists blog_comments (
  comment_id varchar(120) primary key,
  post_id varchar(120) not null,
  author_user_id varchar(120) not null,
  content text not null,
  status varchar(40) not null,
  created_at timestamp with time zone not null,
  constraint fk_blog_comments_post foreign key (post_id) references blog_posts(post_id),
  constraint fk_blog_comments_author foreign key (author_user_id) references users(user_id)
);

create index if not exists idx_blog_comments_post_created_at
  on blog_comments(post_id, created_at asc);

create table if not exists blog_likes (
  like_id varchar(120) primary key,
  post_id varchar(120) not null,
  user_id varchar(120) not null,
  created_at timestamp with time zone not null,
  constraint fk_blog_likes_post foreign key (post_id) references blog_posts(post_id),
  constraint fk_blog_likes_user foreign key (user_id) references users(user_id),
  constraint uq_blog_likes_post_user unique (post_id, user_id)
);

create index if not exists idx_blog_likes_post_created_at
  on blog_likes(post_id, created_at asc);

create table if not exists reviews (
  review_id varchar(120) primary key,
  author_user_id varchar(120) not null,
  resource_type varchar(40) not null,
  resource_id varchar(120) not null,
  order_id varchar(120) not null,
  order_item_id varchar(120) not null,
  rating integer not null,
  title varchar(200) not null,
  content text not null,
  status varchar(40) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint fk_reviews_author foreign key (author_user_id) references users(user_id),
  constraint fk_reviews_order foreign key (order_id) references orders(order_id),
  constraint fk_reviews_order_item foreign key (order_item_id) references order_line_items(order_item_id),
  constraint uq_reviews_author_order_item unique (author_user_id, order_item_id),
  constraint ck_reviews_rating_range check (rating >= 1 and rating <= 5)
);

create index if not exists idx_reviews_resource_created_at
  on reviews(resource_type, resource_id, created_at desc);

create index if not exists idx_reviews_author_created_at
  on reviews(author_user_id, created_at desc);
