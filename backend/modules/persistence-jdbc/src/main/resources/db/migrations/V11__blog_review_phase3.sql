create table if not exists blog_post_images (
  image_id varchar(120) primary key,
  post_id varchar(120) not null,
  public_url text not null,
  original_file_name varchar(255) not null,
  sort_order integer not null,
  created_at timestamp with time zone not null,
  constraint fk_blog_post_images_post foreign key (post_id) references blog_posts(post_id)
);

create index if not exists idx_blog_post_images_post_sort_order
  on blog_post_images(post_id, sort_order asc, created_at asc);

create table if not exists review_images (
  image_id varchar(120) primary key,
  review_id varchar(120) not null,
  public_url text not null,
  original_file_name varchar(255) not null,
  sort_order integer not null,
  created_at timestamp with time zone not null,
  constraint fk_review_images_review foreign key (review_id) references reviews(review_id)
);

create index if not exists idx_review_images_review_sort_order
  on review_images(review_id, sort_order asc, created_at asc);
