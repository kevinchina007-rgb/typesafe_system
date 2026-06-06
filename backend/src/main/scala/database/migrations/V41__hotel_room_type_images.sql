alter table hotel_room_types
  add column if not exists image_url varchar(500);

update hotel_room_types
set image_url = case
  when name ilike '%套房%' or name ilike '%suite%' then '/images/home-hero-candidates/12_夜晚都市_中国上海_灯火把黄浦江写成诗.jpg'
  when name ilike '%双床%' or name ilike '%twin%' then '/images/home-hero-candidates/05_公路_冰岛Road 1_沿着风的方向继续出发.jpg'
  else '/images/home-hero-candidates/04_大山_瑞士Oeschinensee_湖光把山色轻轻收藏.jpg'
end
where image_url is null or trim(image_url) = '';
