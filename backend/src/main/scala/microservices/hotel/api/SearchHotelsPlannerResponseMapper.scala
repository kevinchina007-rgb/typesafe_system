package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*
import com.typesafe.travel.shared.kernel.StayPeriod

object SearchHotelsPlannerResponseMapper:
  def toHotelListPlannerResponse(hotels: List[Hotel], stayPeriod: Option[StayPeriod]): HotelListPlannerResponse =
    HotelListPlannerResponse(hotels.map(hotel => HotelPlannerResponseMapperSupport.toHotelPlannerResponse(hotel, stayPeriod)))
