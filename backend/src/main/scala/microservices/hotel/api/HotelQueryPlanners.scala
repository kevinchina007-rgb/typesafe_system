package com.typesafe.travel.hotel.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.hotel.HotelPlannerPlainSql
import com.typesafe.travel.shared.kernel.{RoomCount, StayPeriod}

import java.sql.Connection
import java.time.LocalDate

object HotelSuggestionsPlanner extends ConnectionApiPlan[HotelSuggestionRequest, SearchSuggestionListPlannerResponse]:
  override val name: String = "HotelSuggestionsPlanner"

  override def plan(input: HotelSuggestionRequest, connection: Connection): IO[SearchSuggestionListPlannerResponse] =
    HotelPlannerPlainSql.suggestions(connection, input)

object SearchHotelsPlanner extends ConnectionApiPlan[HotelSearchRequest, HotelListPlannerResponse]:
  override val name: String = "SearchHotelsPlanner"

  override def plan(input: HotelSearchRequest, connection: Connection): IO[HotelListPlannerResponse] =
    HotelPlannerPlainSql.list(connection, input)

object GetHotelDetailsPlanner extends ConnectionApiPlan[HotelDetailsRequest, HotelPlannerResponse]:
  override val name: String = "GetHotelDetailsPlanner"

  override def plan(input: HotelDetailsRequest, connection: Connection): IO[HotelPlannerResponse] =
    HotelPlannerPlainSql.details(connection, input)

object BookHotelPlanner extends ConnectionApiPlan[BookHotelPlannerRequest, HotelBookingPlannerResponse]:
  override val name: String = "BookHotelPlanner"

  override def plan(input: BookHotelPlannerRequest, connection: Connection): IO[HotelBookingPlannerResponse] =
    for
      _ <- IO.fromEither(StayPeriod.create(LocalDate.parse(input.checkInDate), LocalDate.parse(input.checkOutDate)))
      roomCount <- IO.fromEither(RoomCount.create(input.roomCount))
      _ <- if roomCount.value > 0 then IO.unit else IO.raiseError(new IllegalArgumentException("Room count must be greater than zero"))
      _ <- if input.guestTravelerIds.nonEmpty then IO.unit else IO.raiseError(new IllegalArgumentException("At least one guest traveler is required for hotel booking"))
      response <- HotelPlannerPlainSql.book(connection, input, java.time.Instant.now())
    yield response
