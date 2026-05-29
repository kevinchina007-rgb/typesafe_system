create table if not exists blog_post_cities (
  post_id varchar(120) not null,
  city_name varchar(120) not null,
  sort_order integer not null,
  constraint fk_blog_post_cities_post foreign key (post_id) references blog_posts(post_id)
);

create unique index if not exists uq_blog_post_cities_post_city
  on blog_post_cities(post_id, city_name);

insert into users(user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at)
values ('user-kapiba-demo', 'kapiba@la.com', '11111111111', U&'\5361\76AE\5DF4\62C9', '/images/avatar-defaults/bara-avatar-1.png', 'Basic', 9999, 'Active', null, timestamp with time zone '2026-05-28 08:00:00+08')
on conflict (email) do update set
  nickname = excluded.nickname,
  avatar_url = excluded.avatar_url,
  membership_level = excluded.membership_level,
  points = excluded.points,
  status = excluded.status;

insert into user_credentials(credential_id, user_id, login_email, password_hash, status, created_at, updated_at, password_updated_at)
select 'credential-kapiba-demo', u.user_id, u.email, 'pbkdf2-sha256$210000$Zmx5YmFyYS1kZW1vLXNhbA==$5L4FJcaqK4loMK59jF7pxfvkCKOBZPsaN/5xDdz78Qk=', 'Active', timestamp with time zone '2026-05-28 08:00:00+08', timestamp with time zone '2026-05-28 08:00:00+08', timestamp with time zone '2026-05-28 08:00:00+08'
from users u
where u.email = 'kapiba@la.com'
on conflict (login_email) do update set
  password_hash = excluded.password_hash,
  status = excluded.status,
  updated_at = excluded.updated_at,
  password_updated_at = excluded.password_updated_at;

with user_seed as (
  select n,
         'blog-user-' || lpad(n::text, 3, '0') as user_id,
         'flybara' || lpad(n::text, 3, '0') || '@la.com' as email,
         '188' || lpad(n::text, 8, '0') as phone,
         (array[U&'\65C5\884C\89C2\5BDF\5458',U&'\9884\7B97\7814\7A76\5458',U&'\62CD\7167\6253\5361\4EBA',U&'\57CE\5E02\6563\6B65\5BB6',U&'\5468\672B\51FA\9003\8005',U&'\673A\573A\5496\5561\6D3E',U&'\70ED\6C64\7231\597D\8005',U&'\4F4E\4EF7\7968\730E\4EBA',U&'\591C\666F\6536\96C6\8005',U&'\6162\6162\8D70\7684\4EBA'])[1 + ((n - 1) % 10)] || n::text as nickname,
         '/images/avatar-defaults/bara-avatar-' || (1 + ((n * 37 + 11) % 8))::text || '.png' as avatar_url
  from generate_series(1, 199) as s(n)
)
insert into users(user_id, email, phone, nickname, avatar_url, membership_level, points, status, default_traveler_id, created_at)
select user_id, email, phone, nickname, avatar_url, 'Basic', 10 + n, 'Active', null, timestamp with time zone '2026-05-28 08:00:00+08' + (n || ' minutes')::interval
from user_seed
on conflict (email) do update set
  nickname = excluded.nickname,
  avatar_url = excluded.avatar_url,
  status = excluded.status;

with user_seed as (
  select u.user_id, u.email, row_number() over (order by u.email) as n
  from users u
  where u.email like 'flybara%@la.com'
)
insert into user_credentials(credential_id, user_id, login_email, password_hash, status, created_at, updated_at, password_updated_at)
select 'credential-' || user_id, user_id, email, 'pbkdf2-sha256$210000$Zmx5YmFyYS1kZW1vLXNhbA==$5L4FJcaqK4loMK59jF7pxfvkCKOBZPsaN/5xDdz78Qk=', 'Active', timestamp with time zone '2026-05-28 08:00:00+08', timestamp with time zone '2026-05-28 08:00:00+08', timestamp with time zone '2026-05-28 08:00:00+08'
from user_seed
on conflict (login_email) do update set
  password_hash = excluded.password_hash,
  status = excluded.status,
  updated_at = excluded.updated_at,
  password_updated_at = excluded.password_updated_at;

delete from blog_notifications;
delete from blog_user_blocks;
delete from blog_user_follows;
delete from blog_comment_likes;
delete from blog_post_tags;
delete from blog_post_cities;
delete from blog_likes;
delete from blog_comments;
delete from blog_post_images;
delete from blog_posts;

with authors as (
  select 0 as n, u.user_id, u.nickname from users u where u.email = 'kapiba@la.com'
  union all
  select row_number() over (order by u.email) as n, u.user_id, u.nickname
  from users u
  where u.email like 'flybara%@la.com'
), posts as (
  select a.n, a.user_id, a.nickname, g as post_no,
         'blog-post-' || lpad(a.n::text, 3, '0') || '-' || g::text as post_id,
         timestamp with time zone '2026-05-20 09:00:00+08' + ((a.n * 17 + g * 19) || ' minutes')::interval as created_at
  from authors a
  cross join lateral generate_series(1, case when a.n = 0 then 5 else 1 + (a.n % 5) end) as g
)
insert into blog_posts(post_id, author_user_id, title, summary, content, cover_text, travel_city, status, created_at, updated_at, published_at)
select post_id,
       user_id,
       replace((array[U&'\4ECA\5929\5728{city}\628A\8DEF\8D70\6210\4E86\4E00\9996\6B4C',U&'\4E09\5C0F\65F6\9003\79BB\8BA1\5212\FF0C\5C45\7136\771F\7684\6709\7528',U&'\628A\9884\7B97\538B\5230\6700\4F4E\4EE5\540E\FF0C\5FEB\4E50\53CD\800C\53D8\5927\4E86',U&'\65E9\73ED\673A/\591C\5E02/\65E7\8857\89D2\FF0C\6211\7684\8FF7\8DEF\7B14\8BB0',U&'\8FD9\5EA7\57CE\5E02\9002\5408\6162\6162\8D70\FF0C\4E0D\9002\5408\8D76\8DEF',U&'\670B\53CB\8BF4\8FD9\8D9F\592A\79BB\8C31\FF0C\4F46\6211\60F3\518D\6765\4E00\6B21',U&'\4EB2\6D4B\4E0D\7D2F\8DEF\7EBF\FF1A\62CD\7167\3001\5403\996D\3001\53D1\5446\90FD\521A\597D',U&'\4E00\4E2A\4EBA\7684\5C0F\65C5\884C\FF0C\4E5F\53EF\4EE5\5F88\70ED\95F9'])[1 + (n % 8)], '{city}', (array[U&'\5317\4EAC',U&'\4E0A\6D77',U&'\6B66\6C49',U&'\5357\4EAC',U&'\676D\5DDE',U&'\6DF1\5733',U&'\91CD\5E86',U&'\5E7F\5DDE',U&'\6210\90FD',U&'\957F\6C99',U&'\53A6\95E8',U&'\897F\5B89'])[1 + ((n + post_no) % 12)]) || ' #' || post_no,
       (array[U&'\5C01\9762\4E0D\662F\6EE4\955C\8BC8\9A97\FF0C\73B0\573A\771F\7684\6709\8FD9\4E48\8212\670D\3002',U&'\7ED9\7B2C\4E00\6B21\6765\7684\4EBA\7559\4E00\4EFD\5C11\8E29\5751\8DEF\7EBF\3002',U&'\5403\5F97\597D\3001\8D70\5F97\5C11\3001\7167\7247\8FD8\633A\80FD\6253\3002',U&'\884C\7A0B\5F88\677E\FF0C\9002\5408\4E34\65F6\8D77\610F\7684\4EBA\3002',U&'\8FD9\7BC7\91CD\70B9\662F\7701\94B1\FF0C\4E0D\662F\7701\5FEB\4E50\3002',U&'\628A\788E\7247\65F6\95F4\62FC\6210\4E86\4E00\8D9F\5C0F\65C5\884C\3002'])[1 + (n % 6)],
       (array[U&'\7B2C\4E00\6BB5\5148\8BF4\7ED3\8BBA\FF1A\8FD9\8D9F\6700\503C\5F97\7684\662F\628A\8282\594F\653E\6162\3002\4E0A\5348\4E0D\8981\6392\592A\6EE1\FF0C\7559\4E00\70B9\65F6\95F4\7ED9\8857\8FB9\5C0F\5E97\548C\7A81\7136\51FA\73B0\7684\597D\5929\6C14\3002',U&'\8DEF\7EBF\5B89\6392\4E0A\6211\66F4\63A8\8350\5148\53BB\4EBA\5C11\7684\5730\65B9\FF0C\518D\53BB\70ED\95E8\70B9\4F4D\3002\8FD9\6837\7167\7247\5E72\51C0\FF0C\4F53\529B\4E5F\4E0D\4F1A\4E00\4E0B\5B50\88AB\62BD\7A7A\3002',U&'\5403\996D\53EF\4EE5\907F\5F00\6700\70ED\7684\8857\53E3\FF0C\5F80\65C1\8FB9\591A\8D70\4E24\6761\5DF7\5B50\FF0C\4EF7\683C\4F1A\67D4\548C\5F88\591A\FF0C\5473\9053\4E5F\7ECF\5E38\66F4\50CF\672C\5730\4EBA\7684\65E5\5E38\3002',U&'\5982\679C\4F60\4E5F\559C\6B22\8FB9\8D70\8FB9\62CD\FF0C\8BB0\5F97\7ED9\624B\673A\7559\7535\3002\5F88\591A\6F02\4EAE\77AC\95F4\4E0D\662F\666F\70B9\7ED9\7684\FF0C\662F\8F6C\89D2\3001\516C\4EA4\7AD9\548C\508D\665A\7684\706F\3002'])[1 + (n % 4)] || chr(10) || chr(10) || (array[U&'\7B2C\4E00\6BB5\5148\8BF4\7ED3\8BBA\FF1A\8FD9\8D9F\6700\503C\5F97\7684\662F\628A\8282\594F\653E\6162\3002\4E0A\5348\4E0D\8981\6392\592A\6EE1\FF0C\7559\4E00\70B9\65F6\95F4\7ED9\8857\8FB9\5C0F\5E97\548C\7A81\7136\51FA\73B0\7684\597D\5929\6C14\3002',U&'\8DEF\7EBF\5B89\6392\4E0A\6211\66F4\63A8\8350\5148\53BB\4EBA\5C11\7684\5730\65B9\FF0C\518D\53BB\70ED\95E8\70B9\4F4D\3002\8FD9\6837\7167\7247\5E72\51C0\FF0C\4F53\529B\4E5F\4E0D\4F1A\4E00\4E0B\5B50\88AB\62BD\7A7A\3002',U&'\5403\996D\53EF\4EE5\907F\5F00\6700\70ED\7684\8857\53E3\FF0C\5F80\65C1\8FB9\591A\8D70\4E24\6761\5DF7\5B50\FF0C\4EF7\683C\4F1A\67D4\548C\5F88\591A\FF0C\5473\9053\4E5F\7ECF\5E38\66F4\50CF\672C\5730\4EBA\7684\65E5\5E38\3002',U&'\5982\679C\4F60\4E5F\559C\6B22\8FB9\8D70\8FB9\62CD\FF0C\8BB0\5F97\7ED9\624B\673A\7559\7535\3002\5F88\591A\6F02\4EAE\77AC\95F4\4E0D\662F\666F\70B9\7ED9\7684\FF0C\662F\8F6C\89D2\3001\516C\4EA4\7AD9\548C\508D\665A\7684\706F\3002'])[1 + ((n + 1) % 4)] || chr(10) || chr(10) || (array[U&'\7B2C\4E00\6BB5\5148\8BF4\7ED3\8BBA\FF1A\8FD9\8D9F\6700\503C\5F97\7684\662F\628A\8282\594F\653E\6162\3002\4E0A\5348\4E0D\8981\6392\592A\6EE1\FF0C\7559\4E00\70B9\65F6\95F4\7ED9\8857\8FB9\5C0F\5E97\548C\7A81\7136\51FA\73B0\7684\597D\5929\6C14\3002',U&'\8DEF\7EBF\5B89\6392\4E0A\6211\66F4\63A8\8350\5148\53BB\4EBA\5C11\7684\5730\65B9\FF0C\518D\53BB\70ED\95E8\70B9\4F4D\3002\8FD9\6837\7167\7247\5E72\51C0\FF0C\4F53\529B\4E5F\4E0D\4F1A\4E00\4E0B\5B50\88AB\62BD\7A7A\3002',U&'\5403\996D\53EF\4EE5\907F\5F00\6700\70ED\7684\8857\53E3\FF0C\5F80\65C1\8FB9\591A\8D70\4E24\6761\5DF7\5B50\FF0C\4EF7\683C\4F1A\67D4\548C\5F88\591A\FF0C\5473\9053\4E5F\7ECF\5E38\66F4\50CF\672C\5730\4EBA\7684\65E5\5E38\3002',U&'\5982\679C\4F60\4E5F\559C\6B22\8FB9\8D70\8FB9\62CD\FF0C\8BB0\5F97\7ED9\624B\673A\7559\7535\3002\5F88\591A\6F02\4EAE\77AC\95F4\4E0D\662F\666F\70B9\7ED9\7684\FF0C\662F\8F6C\89D2\3001\516C\4EA4\7AD9\548C\508D\665A\7684\706F\3002'])[1 + ((n + 2) % 4)],
       (array[U&'\51FA\53D1\5427\FF0C\4ECA\5929\5F88\9002\5408\6D6A\8D39\5728\8DEF\4E0A',U&'\8FD9\8D9F\5C0F\65C5\884C\FF0C\6BD4\60F3\8C61\4E2D\66F4\4F1A\54C4\4EBA\5F00\5FC3',U&'\4F4E\9884\7B97\4E5F\80FD\73A9\5F97\5F88\4F53\9762',U&'\57CE\5E02\6563\6B65\89C2\5BDF\62A5\544A',U&'\62CD\7167\6253\5361\4F46\4E0D\8D76\573A',U&'\6211\5BA3\5E03\8FD9\91CC\9002\5408\53D1\5446',U&'\5403\9971\4EE5\540E\4E16\754C\90FD\53D8\53EF\7231\4E86'])[1 + (n % 7)],
       (array[U&'\5317\4EAC',U&'\4E0A\6D77',U&'\6B66\6C49',U&'\5357\4EAC',U&'\676D\5DDE',U&'\6DF1\5733',U&'\91CD\5E86',U&'\5E7F\5DDE',U&'\6210\90FD',U&'\957F\6C99',U&'\53A6\95E8',U&'\897F\5B89'])[1 + ((n + post_no) % 12)],
       'Published', created_at, created_at, created_at
from posts
on conflict (post_id) do update set
  title = excluded.title,
  summary = excluded.summary,
  content = excluded.content,
  cover_text = excluded.cover_text,
  travel_city = excluded.travel_city,
  status = excluded.status,
  updated_at = excluded.updated_at,
  published_at = excluded.published_at;

with posts as (
  select post_id, row_number() over (order by post_id) as rn from blog_posts where post_id like 'blog-post-%'
), image_numbers as (
  select p.post_id, p.rn, g as image_no
  from posts p
  cross join lateral generate_series(1, 3 + (p.rn % 4)) as g
)
insert into blog_post_images(image_id, post_id, public_url, original_file_name, sort_order, created_at)
select 'blog-image-' || post_id || '-' || image_no,
       post_id,
       'https://picsum.photos/seed/flybara-' || rn || '-' || image_no || '/900/1200',
       'flybara-' || rn || '-' || image_no || '.jpg',
       image_no - 1,
       timestamp with time zone '2026-05-20 09:00:00+08' + ((rn + image_no) || ' minutes')::interval
from image_numbers
on conflict (image_id) do nothing;

with posts as (
  select post_id, row_number() over (order by post_id) as rn from blog_posts where post_id like 'blog-post-%'
), tag_source as (
  select post_id, 'days' as tag_type, (array[U&'1-2\5929',U&'3-5\5929',U&'6\5929\4EE5\4E0A'])[1 + (rn % 3)] as tag_value from posts
  union all select post_id, 'season', (array[U&'\6625\590F',U&'\79CB\51AC',U&'\8282\5047\65E5'])[1 + ((rn + 1) % 3)] from posts
  union all select post_id, 'companion', (array[U&'\4E00\4E2A\4EBA',U&'\670B\53CB',U&'\4EB2\5B50/\60C5\4FA3'])[1 + ((rn + 2) % 3)] from posts
  union all select post_id, 'style', (array[U&'\7F8E\98DF',U&'\7701\94B1',U&'\62CD\7167\6253\5361'])[1 + ((rn + 3) % 3)] from posts
)
insert into blog_post_tags(post_id, tag_type, tag_value)
select post_id, tag_type, tag_value from tag_source
on conflict do nothing;

with posts as (
  select post_id, row_number() over (order by post_id) as rn, travel_city from blog_posts where post_id like 'blog-post-%'
), city_source as (
  select post_id, travel_city as city_name, 0 as sort_order from posts
  union all
  select post_id, (array[U&'\5317\4EAC',U&'\4E0A\6D77',U&'\6B66\6C49',U&'\5357\4EAC',U&'\676D\5DDE',U&'\6DF1\5733',U&'\91CD\5E86',U&'\5E7F\5DDE',U&'\6210\90FD',U&'\957F\6C99',U&'\53A6\95E8',U&'\897F\5B89'])[1 + ((rn + 5) % 12)] as city_name, 1 from posts where rn % 3 = 0
  union all
  select post_id, (array[U&'\5317\4EAC',U&'\4E0A\6D77',U&'\6B66\6C49',U&'\5357\4EAC',U&'\676D\5DDE',U&'\6DF1\5733',U&'\91CD\5E86',U&'\5E7F\5DDE',U&'\6210\90FD',U&'\957F\6C99',U&'\53A6\95E8',U&'\897F\5B89'])[1 + ((rn + 8) % 12)] as city_name, 2 from posts where rn % 7 = 0
)
insert into blog_post_cities(post_id, city_name, sort_order)
select distinct on (post_id, city_name) post_id, city_name, sort_order from city_source order by post_id, city_name, sort_order
on conflict do nothing;

with kapiba as (select user_id from users where email = 'kapiba@la.com'),
followers as (select user_id, row_number() over (order by email) as rn from users where email like 'flybara%@la.com')
insert into blog_user_follows(follow_id, follower_user_id, target_user_id, created_at)
select 'follow-kapiba-' || rn, user_id, (select user_id from kapiba), timestamp with time zone '2026-05-26 08:00:00+08' + (rn || ' minutes')::interval
from followers
on conflict do nothing;

with users_ranked as (
  select user_id, row_number() over (order by email) as rn from users where email like 'flybara%@la.com'
), pairs as (
  select a.user_id as follower_user_id, b.user_id as target_user_id, a.rn as ar, b.rn as br
  from users_ranked a
  join users_ranked b on a.user_id <> b.user_id and ((a.rn * 7 + b.rn * 11) % 31 = 0)
)
insert into blog_user_follows(follow_id, follower_user_id, target_user_id, created_at)
select 'follow-random-' || ar || '-' || br, follower_user_id, target_user_id, timestamp with time zone '2026-05-26 10:00:00+08' + ((ar + br) || ' minutes')::interval
from pairs
on conflict do nothing;

with kapiba_posts as (select post_id, row_number() over (order by post_id) as pr from blog_posts where author_user_id = (select user_id from users where email = 'kapiba@la.com')),
likers as (select user_id, row_number() over (order by email) as rn from users where email like 'flybara%@la.com')
insert into blog_likes(like_id, post_id, user_id, created_at)
select 'like-kapiba-' || pr || '-' || rn, post_id, user_id, timestamp with time zone '2026-05-27 08:00:00+08' + ((pr * 200 + rn) || ' seconds')::interval
from kapiba_posts cross join likers
on conflict do nothing;

with posts as (
  select p.post_id, p.author_user_id, row_number() over (order by p.post_id) as pr
  from blog_posts p
  where p.author_user_id <> (select user_id from users where email = 'kapiba@la.com')
), users_ranked as (
  select user_id, row_number() over (order by email) as rn from users where email like 'flybara%@la.com' or email = 'kapiba@la.com'
), likes as (
  select p.post_id, u.user_id, p.pr, u.rn
  from posts p
  join users_ranked u on u.user_id <> p.author_user_id and ((p.pr * 13 + u.rn * 17) % 19 in (0, 1, 2))
)
insert into blog_likes(like_id, post_id, user_id, created_at)
select 'like-random-' || pr || '-' || rn, post_id, user_id, timestamp with time zone '2026-05-27 12:00:00+08' + ((pr * 20 + rn) || ' seconds')::interval
from likes
on conflict do nothing;

with kapiba as (select user_id from users where email = 'kapiba@la.com'), actors as (
  select user_id, nickname, row_number() over (order by email) as rn from users where email like 'flybara%@la.com' limit 12
)
insert into blog_notifications(notification_id, receiver_user_id, actor_user_id, notification_type, post_id, comment_id, content, is_read, created_at)
select 'notify-kapiba-' || rn, (select user_id from kapiba), user_id,
       case when rn % 3 = 0 then 'follow' when rn % 3 = 1 then 'like' else 'comment' end,
       (select post_id from blog_posts where author_user_id = (select user_id from kapiba) order by post_id limit 1),
       null,
       nickname || case when rn % 3 = 0 then U&'\5173\6CE8\4E86\4F60' when rn % 3 = 1 then U&'\559C\6B22\4E86\4F60\7684\5E16\5B50' else U&'\8BC4\8BBA\4E86\4F60\7684\65C5\884C\7B14\8BB0' end,
       false,
       timestamp with time zone '2026-05-28 10:00:00+08' + (rn || ' minutes')::interval
from actors
on conflict (notification_id) do update set
  content = excluded.content,
  is_read = excluded.is_read,
  created_at = excluded.created_at;
