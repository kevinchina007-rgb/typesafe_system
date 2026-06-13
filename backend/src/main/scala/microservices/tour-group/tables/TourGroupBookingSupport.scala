package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.attraction.api.BookAttractionItemPlanner
import com.typesafe.travel.attraction.domain.BookAttractionItemPlannerRequest
import com.typesafe.travel.flight.api.BookFlightPlanner
import com.typesafe.travel.flight.objects.BookFlightPlannerRequest
import com.typesafe.travel.hotel.api.BookHotelPlanner
import com.typesafe.travel.hotel.objects.BookHotelPlannerRequest
import com.typesafe.travel.order.domain.CreateOrderPlannerRequest
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.BookTrainItemPlanner
import com.typesafe.travel.train.domain.BookTrainItemPlannerRequest

import java.sql.Connection
import java.time.Instant

object TourGroupBookingSupport:
  def createOrderForSelection(
      connection: Connection,
      userId: String,
      selection: GroupPlanSelection,
      planItem: GroupPlanItem,
      planOption: GroupPlanOption,
      now: Instant
  ): IO[String] =
    planItem.itemType match
      case GroupPlanItemType.Flight =>
        val (departureAirport, arrivalAirport, departureDate) = TourGroupSelectionSupport.parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Flight selection is missing resource context")))
        BookFlightPlanner
          .plan(
          BookFlightPlannerRequest(
            userId = userId,
            flightId = planOption.resourceId,
            travelerIds = selection.travelerIds.map(_.value).toList,
            cabinClass = planOption.resourceVariantCode.getOrElse("Economy")
          ),
          connection
        )
          .map(_.orderId)
      case GroupPlanItemType.Hotel =>
        val (checkInDate, checkOutDate) = TourGroupSelectionSupport.parseStayContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Hotel selection is missing stay context")))
        BookHotelPlanner
          .plan(
          BookHotelPlannerRequest(
            userId = userId,
            roomTypeId = planOption.resourceId,
            guestTravelerIds = selection.travelerIds.map(_.value).toList,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            roomCount = selection.quantity
          ),
          connection
        )
          .map(_.orderId)
      case GroupPlanItemType.Train =>
        val (fromStationCode, toStationCode, date) = TourGroupSelectionSupport.parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Train selection is missing route context")))
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookTrainItemPlanner
            .plan(
              BookTrainItemPlannerRequest(
                userId = userId,
                orderId = order.orderId,
                trainId = planOption.resourceId,
                travelerIds = selection.travelerIds.map(_.value).toList,
                fromStationCode = fromStationCode,
                toStationCode = toStationCode,
                seatClass = planOption.resourceVariantCode.getOrElse("SecondClass"),
                seatPreference = None
              ),
              connection
            )
        yield order.orderId
      case GroupPlanItemType.Attraction =>
        val useDate = planItem.scheduledAt.toString.take(10)
        val attractionId = planOption.resourceContext.map(_.trim).filter(_.nonEmpty).getOrElse {
          throw new IllegalArgumentException("Attraction selection is missing attraction context")
        }
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookAttractionItemPlanner
            .plan(
              BookAttractionItemPlannerRequest(
                userId = userId,
                orderId = order.orderId,
                attractionId = attractionId,
                ticketTypeId = planOption.resourceId,
                sessionId = planOption.resourceVariantCode,
                travelerIds = selection.travelerIds.map(_.value).toList,
                useDate = useDate
              ),
              connection
            )
        yield order.orderId

  def submitSelection(connection: Connection, input: SubmitTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    val loadedContext = IO.blocking {
      val selection = TourGroupSelectionSupport.selectionById(connection, input.selectionId)
      val activeMembershipRow = TourGroupMembershipSupport.activeMembership(connection, selection.groupId.value, input.userId)
      if selection.membershipId != activeMembershipRow.membershipId then
        throw TourGroupError.MembershipScopeDidNotMatch(activeMembershipRow.membershipId, UserId(input.userId))
      val acceptedSelection = submitGroupPlanSelection(selection, now).fold(throw _, identity)
      val planItem = TourGroupSelectionSupport.planItems(connection, selection.groupId.value).find(_.planItemId == selection.planItemId).getOrElse {
        throw TourGroupError.PlanItemWasNotFound(selection.planItemId)
      }
      val planOption = TourGroupSelectionSupport.planOptions(connection, selection.groupId.value).find(_.optionId == selection.optionId).getOrElse {
        throw TourGroupError.PlanOptionWasNotFound(selection.optionId)
      }
      (acceptedSelection, planItem, planOption)
    }.flatMap { case (acceptedSelection, planItem, planOption) =>
      TourGroupSelectionSupport.existingSelectionOrderId(connection, acceptedSelection.selectionId.value).flatMap {
        case Some(_) => IO.blocking(TourGroupMembershipSupport.details(connection, acceptedSelection.groupId.value))
        case None =>
          createOrderForSelection(connection, input.userId, acceptedSelection, planItem, planOption, now).flatMap { orderId =>
            IO.blocking {
              TourGroupSelectionSupport.insertSelectionOrderLink(connection, acceptedSelection.selectionId.value, orderId, now)
              TourGroupSelectionSupport.updateSelectionAsConvertedToOrder(connection, acceptedSelection.selectionId.value, now)
              TourGroupMembershipSupport.details(connection, acceptedSelection.groupId.value)
            }
          }
      }
    }
    loadedContext
