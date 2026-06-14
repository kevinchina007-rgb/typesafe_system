package com.typesafe.travel.hotel.api

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
