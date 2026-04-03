package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.CurrentManagerSessionView
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.{LocalDate, OffsetDateTime}

trait ManagerApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def managerRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "manager" / "airline" / "register" =>
      for
        registerAirlineManagerRequestDto <- request.as[RegisterAirlineManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerAirlineManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerAirlineManagerRequestDto.displayName))
        airlineName <- fromEither(AirlineName.create(registerAirlineManagerRequestDto.airlineName))
        airlineCode <- fromEither(AirlineCode.create(registerAirlineManagerRequestDto.airlineCode))
        createdAt <- currentInstantF
        managerSession <- managerWorkflowApplicationService.registerAirlineManager(
          primaryEmailAddress = primaryEmailAddress,
          displayName = displayName,
          airlineName = airlineName,
          airlineCode = airlineCode,
          createdAt = createdAt
        )
        _ <- authApplicationService.createManagerCredential(
          managerType = com.typesafe.travel.auth.domain.AuthManagerType.Airline,
          managerId = managerSession.managerId,
          loginEmail = primaryEmailAddress,
          rawPassword = registerAirlineManagerRequestDto.password,
          now = createdAt
        )
        response <- Created(ManagerSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "hotel" / "register" =>
      for
        registerHotelManagerRequestDto <- request.as[RegisterHotelManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerHotelManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerHotelManagerRequestDto.displayName))
        hotelName <- fromEither(HotelName.create(registerHotelManagerRequestDto.hotelName))
        hotelLocation <- fromEither(HotelLocation.create(registerHotelManagerRequestDto.location))
        createdAt <- currentInstantF
        managerSession <- managerWorkflowApplicationService.registerHotelManager(
          primaryEmailAddress = primaryEmailAddress,
          displayName = displayName,
          hotelName = hotelName,
          hotelLocation = hotelLocation,
          createdAt = createdAt
        )
        _ <- authApplicationService.createManagerCredential(
          managerType = com.typesafe.travel.auth.domain.AuthManagerType.Hotel,
          managerId = managerSession.managerId,
          loginEmail = primaryEmailAddress,
          rawPassword = registerHotelManagerRequestDto.password,
          now = createdAt
        )
        response <- Created(ManagerSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

    case request @ GET -> Root / "api" / "manager" / "tasks" :? ManagerIdQueryParamMatcher(managerIdValue) +&
        ManagerTypeQueryParamMatcher(managerTypeValue) +&
        TaskStatusQueryParamMatcher(taskStatusValue) +&
        TaskResourceTypeQueryParamMatcher(taskResourceTypeValue) =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(managerTypeValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        _ <- ensureManagerScope(currentManager, managerIdText, managerTypeText)
        requestedManagerType = ManagerDtoMappers.toManagerType(managerTypeText)
        tasks <- managerWorkflowApplicationService.listManagerTasks(
          managerId = ManagerId(managerIdText),
          managerType = requestedManagerType,
          requestedSupplierReviewStatuses = parseRequestedSupplierReviewStatuses(taskStatusValue),
          requestedResourceTypes = ManagerDtoMappers.toRequestedManagerTypes(taskResourceTypeValue, requestedManagerType)
        )
        response <- Ok(ManagerBookingTaskListResponseDto(tasks.map(ManagerBookingTaskResponseDto.fromApplication)).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "tasks" / "batch-confirm" =>
      for
        currentManager <- requireCurrentManager(request)
        managerBatchDecisionRequestDto <- request.as[ManagerBatchDecisionRequestDto]
        _ <- ensureManagerScope(currentManager, managerBatchDecisionRequestDto.managerId, managerBatchDecisionRequestDto.managerType)
        decidedAt <- currentInstantF
        updatedOrders <- managerWorkflowApplicationService.batchConfirmBookingItems(
          managerId = ManagerId(managerBatchDecisionRequestDto.managerId),
          managerType = ManagerDtoMappers.toManagerType(managerBatchDecisionRequestDto.managerType),
          orderItemIds = managerBatchDecisionRequestDto.orderItemIds.map(OrderItemId.apply),
          note = managerBatchDecisionRequestDto.note.map(_.trim).filter(_.nonEmpty),
          decidedAt = decidedAt
        )
        response <- Ok(
          ManagerBatchDecisionResponseDto(
            processedCount = updatedOrders.size,
            orderItemIds = managerBatchDecisionRequestDto.orderItemIds,
            action = "confirm"
          ).asJson
        )
      yield response

    case request @ POST -> Root / "api" / "manager" / "tasks" / "batch-reject" =>
      for
        currentManager <- requireCurrentManager(request)
        managerBatchDecisionRequestDto <- request.as[ManagerBatchDecisionRequestDto]
        _ <- ensureManagerScope(currentManager, managerBatchDecisionRequestDto.managerId, managerBatchDecisionRequestDto.managerType)
        rejectReason <- fromEither(
          managerBatchDecisionRequestDto.reason.map(_.trim).filter(_.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("reason"))
        )
        decidedAt <- currentInstantF
        updatedOrders <- managerWorkflowApplicationService.batchRejectBookingItems(
          managerId = ManagerId(managerBatchDecisionRequestDto.managerId),
          managerType = ManagerDtoMappers.toManagerType(managerBatchDecisionRequestDto.managerType),
          orderItemIds = managerBatchDecisionRequestDto.orderItemIds.map(OrderItemId.apply),
          reason = rejectReason,
          decidedAt = decidedAt
        )
        response <- Ok(
          ManagerBatchDecisionResponseDto(
            processedCount = updatedOrders.size,
            orderItemIds = managerBatchDecisionRequestDto.orderItemIds,
            action = "reject"
          ).asJson
        )
      yield response

    case request @ GET -> Root / "api" / "manager" / "flights" :? ManagerIdQueryParamMatcher(managerIdValue) =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        _ <- ensureManagerScope(currentManager, managerIdText, "airline")
        flights <- managerWorkflowApplicationService.listFlightsForAirlineManager(ManagerId(managerIdText))
        response <- Ok(FlightListResponseDto(flights.map { case (airline, flight) => FlightResponseDto.fromDomain(airline, flight) }).asJson)
      yield response

    case request @ GET -> Root / "api" / "manager" / "refund-tasks" :? ManagerIdQueryParamMatcher(managerIdValue) +&
        ManagerTypeQueryParamMatcher(managerTypeValue) =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(managerTypeValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        _ <- ensureManagerScope(currentManager, managerIdText, managerTypeText)
        tasks <- managerWorkflowApplicationService.listManagerRefundTasks(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText)
        )
        response <- Ok(ManagerRefundTaskListResponseDto(tasks.map(ManagerRefundTaskResponseDto.fromApplication)).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "flights" =>
      for
        currentManager <- requireCurrentManager(request)
        createManagerFlightRequestDto <- request.as[CreateManagerFlightRequestDto]
        _ <- ensureManagerScope(currentManager, createManagerFlightRequestDto.managerId, "airline")
        flightNumber <- fromEither(FlightNumber.create(createManagerFlightRequestDto.flightNumber))
        departureAirport <- fromEither(AirportCode.create(createManagerFlightRequestDto.departureAirport))
        arrivalAirport <- fromEither(AirportCode.create(createManagerFlightRequestDto.arrivalAirport))
        economySeatCount <- fromEither(SeatCount.create(createManagerFlightRequestDto.economySeatCount))
        economyPrice <- fromEither(Money.create(BigDecimal(createManagerFlightRequestDto.economyPrice), OrderDtoMappers.toCurrency(createManagerFlightRequestDto.currency)))
        businessSeatCount <- fromEither(SeatCount.create(createManagerFlightRequestDto.businessSeatCount))
        businessPrice <- fromEither(Money.create(BigDecimal(createManagerFlightRequestDto.businessPrice), OrderDtoMappers.toCurrency(createManagerFlightRequestDto.currency)))
        createdAt <- currentInstantF
        airlineAndFlight <- managerWorkflowApplicationService.createFlightForAirlineManager(
          managerId = ManagerId(createManagerFlightRequestDto.managerId),
          flightNumber = flightNumber,
          departureAirport = departureAirport,
          arrivalAirport = arrivalAirport,
          departureAt = OffsetDateTime.parse(createManagerFlightRequestDto.departureTime),
          arrivalAt = OffsetDateTime.parse(createManagerFlightRequestDto.arrivalTime),
          economySeatCount = economySeatCount,
          economyPrice = economyPrice,
          businessSeatCount = businessSeatCount,
          businessPrice = businessPrice,
          createdAt = createdAt
        )
        response <- Created(FlightResponseDto.fromDomain(airlineAndFlight._1, airlineAndFlight._2).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "hotel-room-types" =>
      for
        currentManager <- requireCurrentManager(request)
        createManagerRoomTypeRequestDto <- request.as[CreateManagerRoomTypeRequestDto]
        _ <- ensureManagerScope(currentManager, createManagerRoomTypeRequestDto.managerId, "hotel")
        roomTypeName <- fromEither(RoomTypeName.create(createManagerRoomTypeRequestDto.roomTypeName))
        roomCapacity <- fromEither(Capacity.create(createManagerRoomTypeRequestDto.capacity))
        bedType <- fromEither(BedType.create(createManagerRoomTypeRequestDto.bedType))
        nightlyPrice <- fromEither(
          Money.create(
            amount = BigDecimal(createManagerRoomTypeRequestDto.nightlyPrice),
            currency = OrderDtoMappers.toCurrency(createManagerRoomTypeRequestDto.currency)
          )
        )
        availableRooms <- fromEither(RoomCount.create(createManagerRoomTypeRequestDto.availableRooms))
        hotel <- managerWorkflowApplicationService.createRoomTypeForHotelManager(
          managerId = ManagerId(createManagerRoomTypeRequestDto.managerId),
          roomTypeName = roomTypeName,
          roomCapacity = roomCapacity,
          bedType = bedType,
          nightlyPrice = nightlyPrice,
          availableRooms = availableRooms,
          inventoryStartDate = LocalDate.parse(createManagerRoomTypeRequestDto.inventoryStartDate),
          inventoryEndDate = LocalDate.parse(createManagerRoomTypeRequestDto.inventoryEndDate)
        )
        response <- Created(HotelResponseDto.fromDomain(hotel, None).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "booking-items" / orderItemValue / "confirm" =>
      for
        currentManager <- requireCurrentManager(request)
        managerDecisionRequestDto <- request.as[ManagerDecisionRequestDto]
        _ <- ensureManagerScope(currentManager, managerDecisionRequestDto.managerId, managerDecisionRequestDto.managerType)
        decidedAt <- currentInstantF
        updatedOrder <- managerWorkflowApplicationService.confirmBookingItem(
          managerId = ManagerId(managerDecisionRequestDto.managerId),
          managerType = ManagerDtoMappers.toManagerType(managerDecisionRequestDto.managerType),
          orderItemId = OrderItemId(orderItemValue),
          note = managerDecisionRequestDto.note.map(_.trim).filter(_.nonEmpty),
          decidedAt = decidedAt
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "booking-items" / orderItemValue / "reject" =>
      for
        currentManager <- requireCurrentManager(request)
        managerDecisionRequestDto <- request.as[ManagerDecisionRequestDto]
        _ <- ensureManagerScope(currentManager, managerDecisionRequestDto.managerId, managerDecisionRequestDto.managerType)
        rejectReason <- fromEither(
          managerDecisionRequestDto.reason.map(_.trim).filter(_.nonEmpty).toRight(OrderError.SupplierRejectReasonWasEmpty(OrderItemId(orderItemValue)))
        )
        decidedAt <- currentInstantF
        updatedOrder <- managerWorkflowApplicationService.rejectBookingItem(
          managerId = ManagerId(managerDecisionRequestDto.managerId),
          managerType = ManagerDtoMappers.toManagerType(managerDecisionRequestDto.managerType),
          orderItemId = OrderItemId(orderItemValue),
          reason = rejectReason,
          decidedAt = decidedAt
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "orders" / orderIdValue / "refund" / "approve" =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(request.params.get("managerId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(request.params.get("managerType").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        _ <- ensureManagerScope(currentManager, managerIdText, managerTypeText)
        decidedAt <- currentInstantF
        order <- managerWorkflowApplicationService.approveRefund(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText),
          orderId = OrderId(orderIdValue),
          decidedAt = decidedAt
        )
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "orders" / orderIdValue / "refund" / "reject" =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(request.params.get("managerId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(request.params.get("managerType").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        _ <- ensureManagerScope(currentManager, managerIdText, managerTypeText)
        order <- managerWorkflowApplicationService.rejectRefund(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText),
          orderId = OrderId(orderIdValue)
        )
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(orderResponseDto.asJson)
      yield response
  }

  private def ensureManagerScope(currentManager: CurrentManagerSessionView, managerIdValue: String, managerTypeValue: String): F[Unit] =
    val requestedType = managerTypeValue.trim.toLowerCase match
      case "hotel"      => com.typesafe.travel.auth.domain.AuthManagerType.Hotel
      case "train"      => com.typesafe.travel.auth.domain.AuthManagerType.Train
      case "attraction" => com.typesafe.travel.auth.domain.AuthManagerType.Attraction
      case _             => com.typesafe.travel.auth.domain.AuthManagerType.Airline
    if currentManager.managerId == ManagerId(managerIdValue) && currentManager.managerType == requestedType then Async[F].unit
    else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
