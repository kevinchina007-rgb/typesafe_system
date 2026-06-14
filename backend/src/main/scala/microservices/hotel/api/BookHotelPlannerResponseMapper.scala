package com.typesafe.travel.hotel.api

// 这个 mapper 只服务于后端的预订链路，把订单和订单项信息转换为 `HotelBookingPlannerResponse`。
// 前端不需要镜像它，因为前端已经有 `BookHotelPlanner.ts` 和对应 request/response type 作为跨端契约，
// 而这里的逻辑只是后端内部把数据库写入结果整理成稳定返回值的最后一步。

import com.typesafe.travel.hotel.objects.HotelBookingPlannerResponse

object BookHotelPlannerResponseMapper:
  def toHotelBookingPlannerResponse(
      orderId: String,
      orderItemId: String,
      status: String,
      totalPriceAmount: String,
      currency: String
  ): HotelBookingPlannerResponse =
    HotelBookingPlannerResponse(orderId, orderItemId, status, totalPriceAmount, currency)
