package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.StayPeriod

object GetHotelDetailsPlannerResponseMapper:
  def toHotelPlannerResponse(hotel: Hotel, stayPeriod: Option[StayPeriod]): HotelPlannerResponse =
    HotelPlannerResponseMapperSupport.toHotelPlannerResponse(hotel, stayPeriod)
