package com.typesafe.travel.api.application

import cats.effect.IO
import cats.effect.kernel.Clock
import cats.syntax.all.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerProfileRepository

import java.time.LocalDate

final case class FlightBookingApplicationError(code: String, message: String) extends DomainError

def flightOrderWasNotOwnedByUser(orderId: OrderId, actingUserId: UserId): FlightBookingApplicationError =
  FlightBookingApplicationError(
    code = "flight_order_not_owned_by_user",
    message = s"Order '${orderId.value}' does not belong to user '${actingUserId.value}'"
  )

def flightTravelerSelectionWasInvalid(reason: String): FlightBookingApplicationError =
  FlightBookingApplicationError(
    code = "flight_traveler_selection_invalid",
    message = reason
  )

def flightCabinWasNotFound(flightId: FlightId, cabinClass: CabinClass): FlightBookingApplicationError =
  FlightBookingApplicationError(
    code = "flight_cabin_not_found",
    message = s"Flight '${flightId.value}' does not have a cabin '${cabinClass.value}'"
  )

def flightCabinWasNotBookable(flightId: FlightId, cabinClass: CabinClass): FlightBookingApplicationError =
  FlightBookingApplicationError(
    code = "flight_cabin_not_bookable",
    message = s"Flight '${flightId.value}' cabin '${cabinClass.value}' is not currently bookable"
  )

def flightRequiresManualPricingError(
    flightId: FlightId,
    surchargeAmount: BigDecimal,
    currency: Currency
): FlightBookingApplicationError =
  FlightBookingApplicationError(
    code = "flight_requires_manual_pricing",
    message =
      s"Flight '${flightId.value}' departs within 48 hours and requires a late-booking surcharge of $surchargeAmount ${currency.toString}"
  )

final class FlightBookingApplicationService(
    flightService: FlightService[IO],
    flightRepository: FlightRepository[IO],
    flightInventoryLockingService: FlightInventoryLockingService[IO],
    orderRepository: OrderRepository[IO],
    travelerProfileRepository: TravelerProfileRepository[IO]
):
  private val lateBookingThresholdHours = 48L

  private def currentInstantF: IO[java.time.Instant] =
    Clock[IO].realTimeInstant

  def browseFlights(
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String],
      departureDate: Option[LocalDate]
  ): IO[List[(Airline, Flight)]] =
    currentInstantF.flatMap { currentTime =>
      flightService
        .browseFlights(None, None, departureDate)
        .map(_.filter(flight => isFlightVisibleInSearch(flight, currentTime)))
        .map(_.filter(flightMatchesSearch(_, departureAirportQuery, arrivalAirportQuery)))
        .map(_.sortBy(flight => -flightSearchScore(flight, departureAirportQuery, arrivalAirportQuery)))
        .flatMap(_.traverse(toAirlineFlightTuple))
    }

  def suggestFlights(keyword: String): IO[List[SearchSuggestion]] =
    SearchRanking.usableKeyword(keyword) match
      case None => IO.pure(List.empty)
      case Some(normalizedKeyword) =>
        currentInstantF.flatMap { currentTime =>
          flightService
            .browseFlights(None, None, None)
            .map(_.filter(flight => isFlightVisibleInSearch(flight, currentTime)))
            .flatMap(_.traverse(toAirlineFlightTuple))
            .map(
              _.flatMap { case (airline, flight) =>
                val departureScore = SearchRanking.weightedScore(
                  normalizedKeyword,
                  flight.departureAirport.value -> 3,
                  flight.arrivalAirport.value -> 1,
                  airline.airlineName.value -> 1,
                  flight.flightNumber.value -> 2
                )
                val arrivalScore = SearchRanking.weightedScore(
                  normalizedKeyword,
                  flight.arrivalAirport.value -> 3,
                  flight.departureAirport.value -> 1,
                  airline.airlineName.value -> 1,
                  flight.flightNumber.value -> 2
                )
                val flightScore = SearchRanking.weightedScore(
                  normalizedKeyword,
                  flight.flightNumber.value -> 4,
                  airline.airlineName.value -> 2,
                  airline.airlineCode.value -> 2
                )

                List(
                  Option.when(departureScore > 0)(
                    SearchSuggestion(
                      resourceType = SearchResourceType.Flight,
                      value = flight.departureAirport.value,
                      title = s"${flight.departureAirport.value} airport",
                      subtitle = s"${airline.airlineName.value} ${flight.flightNumber.value}",
                      score = departureScore
                    )
                  ),
                  Option.when(arrivalScore > 0)(
                    SearchSuggestion(
                      resourceType = SearchResourceType.Flight,
                      value = flight.arrivalAirport.value,
                      title = s"${flight.arrivalAirport.value} airport",
                      subtitle = s"${airline.airlineName.value} ${flight.flightNumber.value}",
                      score = arrivalScore
                    )
                  ),
                  Option.when(flightScore > 0)(
                    SearchSuggestion(
                      resourceType = SearchResourceType.Flight,
                      value = flight.flightNumber.value,
                      title = s"${airline.airlineName.value} ${flight.flightNumber.value}",
                      subtitle = s"${flight.departureAirport.value} -> ${flight.arrivalAirport.value}",
                      score = flightScore
                    )
                  )
                ).flatten
              }
            )
            .map(suggestions => SearchRanking.topDistinctByValue(suggestions, 8))
        }

  def getFlightDetails(flightId: FlightId): IO[(Airline, Flight)] =
    flightService.getFlightDetails(flightId).flatMap(toAirlineFlightTuple)

  def createFlightOrder(
      actingUserId: UserId,
      flightId: FlightId,
      travelerIds: List[TravelerId],
      cabinClass: CabinClass
  ): IO[Order] =
    for
      validatedTravelerIds <- validateTravelerSelection(travelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      ownerOrders <- orderRepository.findOrdersByOwnerUserId(actingUserId)
      flight <- flightService.getFlightDetails(flightId)
      currentTime <- currentInstantF
      _ <- ensureFlightCanBeBookedOnline(flight, currentTime)
      _ <- ensureTravelersDoNotAlreadyHoldFlightTickets(flight.flightId, travelerProfiles.map(_.travelerId), ownerOrders)
      airline <- loadAirlineOrRaise(flight.airlineId)
      cabinInventory <- loadBookableCabinOrRaise(flight, cabinClass)
      generatedOrderId <- orderRepository.nextOrderId
      generatedOrderItemId <- orderRepository.nextOrderItemId
      createdAt <- currentInstantF
      draftOrder = newDraftOrder(
        orderId = generatedOrderId,
        ownerUserId = actingUserId,
        orderCurrency = cabinInventory.unitPrice.currency,
        createdAt = createdAt
      )
      _ <- orderRepository.saveOrder(draftOrder)
      savedOrder <- (
        for
          _ <- flightInventoryLockingService.acquireFlightCabinReservation(
            cabinInventoryId = cabinInventory.cabinInventoryId,
            orderId = generatedOrderId,
            orderItemId = generatedOrderItemId,
            quantity = travelerProfiles.size,
            capacityQuantity = cabinInventory.availableSeats.value,
            reservedAt = createdAt
          )
          orderWithItem <- draftOrder
            .addFlightOrderItem(
              orderItemId = generatedOrderItemId,
              flightBookingSnapshot = FlightBookingSnapshot(
                airlineId = airline.airlineId,
                airlineName = airline.airlineName,
                airlineCode = airline.airlineCode,
                flightId = flight.flightId,
                flightNumber = flight.flightNumber,
                flightSchedule = flight.flightSchedule,
                departureAirportCode = flight.departureAirport,
                arrivalAirportCode = flight.arrivalAirport,
                cabinClass = cabinInventory.cabinClass,
                travelerIds = travelerProfiles.map(_.travelerId).toVector,
                unitPriceSnapshot = cabinInventory.unitPrice
              )
            )
            .liftTo[IO]
          persistedOrder <- orderRepository.saveOrder(orderWithItem)
        yield persistedOrder
      ).handleErrorWith { throwable =>
        orderRepository.deleteOrder(generatedOrderId) *> IO.raiseError(throwable)
      }
    yield savedOrder

  private def toAirlineFlightTuple(flight: Flight): IO[(Airline, Flight)] =
    loadAirlineOrRaise(flight.airlineId).map(_ -> flight)

  private def loadAirlineOrRaise(airlineId: AirlineId): IO[Airline] =
    flightRepository
      .findAirlineById(airlineId)
      .flatMap(_.liftTo[IO](FlightError.airlineWasNotFound(airlineId)))

  private def loadBookableCabinOrRaise(
      flight: Flight,
      requestedCabinClass: CabinClass
  ): IO[CabinInventory] =
    flight.ensureBookableCabinInventory(requestedCabinClass) match
      case Right(cabinInventory) => IO.pure(cabinInventory)
      case Left(flightError)     => IO.raiseError(mapFlightError(flight.flightId, flightError, requestedCabinClass))

  private def flightMatchesSearch(
      flight: Flight,
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String]
  ): Boolean =
    departureAirportQuery.forall(queryText =>
      TravelSearchAliases.hasUsableKeyword(queryText) && TravelSearchAliases.matchesAirportQuery(flight.departureAirport, queryText)
    ) &&
      arrivalAirportQuery.forall(queryText =>
        TravelSearchAliases.hasUsableKeyword(queryText) && TravelSearchAliases.matchesAirportQuery(flight.arrivalAirport, queryText)
      )

  private def flightSearchScore(
      flight: Flight,
      departureAirportQuery: Option[String],
      arrivalAirportQuery: Option[String]
  ): Int =
    departureAirportQuery.map(queryText => SearchRanking.weightedScore(queryText, flight.departureAirport.value -> 4)).getOrElse(0) +
      arrivalAirportQuery.map(queryText => SearchRanking.weightedScore(queryText, flight.arrivalAirport.value -> 4)).getOrElse(0)

  private def isFlightVisibleInSearch(flight: Flight, currentTime: java.time.Instant): Boolean =
    flight.flightSchedule.departureAt.toInstant.isAfter(currentTime)

  private def requiresLateBookingSurcharge(flight: Flight, currentTime: java.time.Instant): Boolean =
    val departureInstant = flight.flightSchedule.departureAt.toInstant
    departureInstant.isAfter(currentTime) && departureInstant.isBefore(currentTime.plusSeconds(lateBookingThresholdHours * 3600))

  private def ensureFlightCanBeBookedOnline(flight: Flight, currentTime: java.time.Instant): IO[Unit] =
    if requiresLateBookingSurcharge(flight, currentTime) then
      val surchargeAmount = (flight.basePrice.amount * BigDecimal("0.15")).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      IO.raiseError(flightRequiresManualPricingError(flight.flightId, surchargeAmount, flight.basePrice.currency))
    else IO.unit

  private def validateTravelerSelection(travelerIds: List[TravelerId]): IO[List[TravelerId]] =
    if travelerIds.isEmpty then
      IO.raiseError(flightTravelerSelectionWasInvalid("At least one traveler must be selected"))
    else if travelerIds.distinct.size != travelerIds.size then
      IO.raiseError(flightTravelerSelectionWasInvalid("Traveler selection contains duplicates"))
    else IO.pure(travelerIds)

  private def loadOwnedTravelerProfile(
      actingUserId: UserId,
      travelerId: TravelerId
  ): IO[com.typesafe.travel.traveler.domain.TravelerProfile] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case Some(travelerProfile) if travelerProfile.ownerUserId == actingUserId =>
        IO.pure(travelerProfile)
      case _ =>
        IO.raiseError(
          flightTravelerSelectionWasInvalid(
            s"Traveler '${travelerId.value}' is not available for user '${actingUserId.value}'"
          )
        )
    }

  private def ensureTravelersDoNotAlreadyHoldFlightTickets(
      flightId: FlightId,
      travelerIds: List[TravelerId],
      ownerOrders: List[Order]
  ): IO[Unit] =
    ownerOrders
      .flatMap(_.orderLineItems)
      .collect { case flightOrderItem: FlightOrderItem => flightOrderItem }
      .find(flightOrderItem =>
        flightOrderItem.flightBookingSnapshot.flightId == flightId &&
          flightOrderItem.orderItemStatus != OrderItemStatus.Cancelled &&
          flightOrderItem.orderItemStatus != OrderItemStatus.Refunded &&
          flightOrderItem.flightBookingSnapshot.travelerIds.exists(travelerIds.contains)
      ) match
      case Some(conflictingOrderItem) =>
        val conflictingTravelerId =
          conflictingOrderItem.flightBookingSnapshot.travelerIds.find(travelerIds.contains).getOrElse(travelerIds.head)
        IO.raiseError(FlightError.flightTravelerWasAlreadyBooked(flightId, conflictingTravelerId))
      case None =>
        IO.unit

  private def mapFlightError(
      flightId: FlightId,
      flightError: FlightError,
      requestedCabinClass: CabinClass
  ): DomainError =
    flightError.code match
      case "flight_cabin_not_found" =>
        flightCabinWasNotFound(flightId, requestedCabinClass)
      case "flight_cabin_not_bookable" | "flight_not_open_for_booking" =>
        flightCabinWasNotBookable(flightId, requestedCabinClass)
      case _ =>
        flightError
