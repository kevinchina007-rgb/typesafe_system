alter table attractions
  add column if not exists image_url varchar(500);

update attractions
set image_url = case
  when attraction_id like 'demo-attraction-%-ocean-park' then '/images/home-hero-candidates/01_大海_葡萄牙Praia da Marinha_海与岩壁在这里相爱.jpg'
  when attraction_id like 'demo-attraction-%-museum' then '/images/home-hero-candidates/09_白昼都市_日本东京_在白昼的楼宇间重新出发.jpg'
  when attraction_id like 'demo-attraction-%-garden' then '/images/home-hero-candidates/04_大山_瑞士Oeschinensee_湖光把山色轻轻收藏.jpg'
  when attraction_id like 'demo-attraction-%-theme-park' then '/images/home-hero-candidates/11_白昼都市_美国西雅图_晨光落在每一段旅程上.jpg'
  when attraction_id like 'demo-attraction-%-night-tour' then '/images/home-hero-candidates/12_夜晚都市_中国上海_灯火把黄浦江写成诗.jpg'
  when attraction_id like 'demo-attraction-%-science' then '/images/home-hero-candidates/10_白昼都市_中国香港_海风也穿过城市.jpg'
  when attraction_id like 'demo-attraction-%-art' then '/images/home-hero-candidates/13_夜晚都市_美国洛杉矶_夜色仍在奔赴远方.jpg'
  when attraction_id like 'demo-attraction-%-ancient' then '/images/home-hero-candidates/03_大山_瑞士Matterhorn_群山把黄昏留给旅人.jpg'
  else image_url
end
where image_url is null or trim(image_url) = '';
