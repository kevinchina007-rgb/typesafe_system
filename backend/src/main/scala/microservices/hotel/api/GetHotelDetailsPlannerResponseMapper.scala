package com.typesafe.travel.hotel.api

// 这个 mapper 是酒店详情链路的后端组装层，负责把酒店基础信息、房型列表和库存区间合并成详情响应。
// 它不应该出现在前端，因为前端只关心 `GetHotelDetailsPlanner` 的请求和 response 结构，不关心后端多表查询如何拼接。
// 保持这层在后端，可以让详情页的输出稳定，同时不把数据库结构泄漏到 UI 层。

import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.StayPeriod

object GetHotelDetailsPlannerResponseMapper:
  def toHotelPlannerResponse(hotel: Hotel, stayPeriod: Option[StayPeriod]): HotelPlannerResponse =
    HotelPlannerResponseMapperSupport.toHotelPlannerResponse(hotel, stayPeriod)
