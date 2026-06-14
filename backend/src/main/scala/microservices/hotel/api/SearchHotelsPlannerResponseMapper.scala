package com.typesafe.travel.hotel.api

// 这个 mapper 只负责酒店搜索结果的后端映射，把命中的酒店记录转成列表响应。
// 前端不镜像它，因为前端不参与搜索 SQL、去重、排序和结果聚合；前端只消费搜索 planner 返回的 JSON。
// 这类文件存在的目的，是把“怎么查”和“怎么展示”隔离开，避免把查询实现细节泄漏到前端。

import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.StayPeriod

object SearchHotelsPlannerResponseMapper:
  def toHotelListPlannerResponse(hotels: List[Hotel], stayPeriod: Option[StayPeriod]): HotelListPlannerResponse =
    HotelListPlannerResponse(hotels.map(hotel => HotelPlannerResponseMapperSupport.toHotelPlannerResponse(hotel, stayPeriod)))
