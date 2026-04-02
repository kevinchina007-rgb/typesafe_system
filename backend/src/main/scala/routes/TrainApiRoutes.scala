package com.typesafe.travel.api.routes

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.Instant

trait TrainApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def trainRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "train-admin" / "managers" =>
      for
        registerRailwayManagerRequestDto <- request.as[RegisterRailwayManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerRailwayManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerRailwayManagerRequestDto.displayName))
        createdAt <- currentInstantF
        managerSession <- trainAdminApplicationService.registerRailwayManager(
          operatorCode = registerRailwayManagerRequestDto.operatorCode,
          primaryEmailAddress = primaryEmailAddress,
          displayName = displayName,
          createdAt = createdAt
        )
        _ <- authApplicationService.createManagerCredential(
          managerType = com.typesafe.travel.auth.domain.AuthManagerType.Train,
          managerId = managerSession.railwayManager.managerId,
          loginEmail = primaryEmailAddress,
          rawPassword = registerRailwayManagerRequestDto.password,
          now = createdAt
        )
        response <- Created(TrainAdminSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

    case request @ GET -> Root / "api" / "train-admin" / "trains" :? ManagerIdQueryParamMatcher(managerIdValue) =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Train && currentManager.managerId == ManagerId(managerIdText) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        trains <- trainAdminApplicationService.listManagedTrains(ManagerId(managerIdText))
        currentTime <- currentInstantF
        trainResponseDtos <- trains.traverse(train => loadRemainingTrainSeats(train, currentTime).map(remainingSeats => TrainResponseDto.fromDomain(train, remainingSeats)))
        response <- Ok(TrainListResponseDto(trainResponseDtos).asJson)
      yield response

    case request @ POST -> Root / "api" / "train-admin" / "trains" =>
      for
        currentManager <- requireCurrentManager(request)
        createTrainJourneyRequestDto <- request.as[CreateTrainJourneyRequestDto]
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Train && currentManager.managerId == ManagerId(createTrainJourneyRequestDto.managerId) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        trainNumber <- fromEither(TrainDtoMappers.toTrainNumber(createTrainJourneyRequestDto.trainNumber))
        stops <- createTrainJourneyRequestDto.stops.traverse { stopRequestDto =>
          for
            stationCode <- fromEither(TrainDtoMappers.toTrainStationCode(stopRequestDto.stationCode))
            stationName <- fromEither(TrainDtoMappers.toTrainStationName(stopRequestDto.stationName))
          yield CreateTrainStopInput(
            stationCode = stationCode,
            stationName = stationName,
            arrivalTime = stopRequestDto.arrivalTime.map(Instant.parse),
            departureTime = stopRequestDto.departureTime.map(Instant.parse)
          )
        }
        seatInventories <- createTrainJourneyRequestDto.seatInventories.traverse { seatInventoryRequestDto =>
          for
            seatClass <- fromEither(TrainDtoMappers.toTrainSeatClass(seatInventoryRequestDto.seatClass))
            totalSeats <- fromEither(SeatCount.create(seatInventoryRequestDto.totalSeats))
            saleableSeats <- fromEither(SeatCount.create(seatInventoryRequestDto.saleableSeats))
          yield CreateTrainSeatInventoryInput(seatClass, totalSeats, saleableSeats)
        }
        segmentPrices <- createTrainJourneyRequestDto.segmentPrices.traverse { segmentPriceRequestDto =>
          for
            fromStationCode <- fromEither(TrainDtoMappers.toTrainStationCode(segmentPriceRequestDto.fromStationCode))
            toStationCode <- fromEither(TrainDtoMappers.toTrainStationCode(segmentPriceRequestDto.toStationCode))
            seatClass <- fromEither(TrainDtoMappers.toTrainSeatClass(segmentPriceRequestDto.seatClass))
            price <- fromEither(Money.create(BigDecimal(segmentPriceRequestDto.amount), TrainDtoMappers.toCurrency(segmentPriceRequestDto.currency)))
          yield CreateTrainSegmentPriceInput(fromStationCode, toStationCode, seatClass, price)
        }
        refundPolicies <- createTrainJourneyRequestDto.refundPolicies.traverse { refundPolicyRequestDto =>
          for
            refundRate <- fromEither(TrainDtoMappers.toRefundRate(refundPolicyRequestDto.refundRate))
          yield CreateTrainRefundPolicyInput(
            startOffsetBeforeDeparture = TrainDtoMappers.toOffsetDuration(refundPolicyRequestDto.startOffsetMinutesBeforeDeparture),
            endOffsetBeforeDeparture = TrainDtoMappers.toOffsetDuration(refundPolicyRequestDto.endOffsetMinutesBeforeDeparture),
            refundType = TrainDtoMappers.toTrainRefundType(refundPolicyRequestDto.refundType),
            refundRate = refundRate
          )
        }
        createdAt <- currentInstantF
        trainJourney <- trainAdminApplicationService.createTrainJourney(
          managerId = ManagerId(createTrainJourneyRequestDto.managerId),
          trainNumber = trainNumber,
          saleStartsAt = Instant.parse(createTrainJourneyRequestDto.saleStartsAt),
          stops = stops,
          seatConfigs = seatInventories,
          segmentPrices = segmentPrices,
          refundPolicies = refundPolicies,
          createdAt = createdAt
        )
        response <- Created(TrainResponseDto.fromDomain(trainJourney).asJson)
      yield response

    case GET -> Root / "api" / "trains" :? FromStationQueryParamMatcher(fromStationValue) +&
        ToStationQueryParamMatcher(toStationValue) +&
        DepartureDateQueryParamMatcher(departureDateValue) =>
      for
        fromStationQuery <- parseOptionalSearchText(fromStationValue)
        toStationQuery <- parseOptionalSearchText(toStationValue)
        departureDate <- parseOptionalDate(departureDateValue)
        trains <- trainBookingApplicationService.browseTrains(fromStationQuery, toStationQuery, departureDate)
        currentTime <- currentInstantF
        trainResponseDtos <- trains.traverse(train => loadRemainingTrainSeats(train, currentTime).map(remainingSeats => TrainResponseDto.fromDomain(train, remainingSeats)))
        response <- Ok(TrainListResponseDto(trainResponseDtos).asJson)
      yield response

    case GET -> Root / "api" / "trains" / trainIdValue =>
      trainBookingApplicationService
        .getTrainDetails(TrainId(trainIdValue))
        .flatMap(trainJourney =>
          currentInstantF.flatMap(currentTime => loadRemainingTrainSeats(trainJourney, currentTime).flatMap(remainingSeats => Ok(TrainResponseDto.fromDomain(trainJourney, remainingSeats).asJson)))
        )

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "train-items" =>
      for
        currentUserId <- requireCurrentUserId(request)
        bookTrainItemRequestDto <- request.as[BookTrainItemRequestDto]
        fromStationCode <- fromEither(TrainDtoMappers.toTrainStationCode(bookTrainItemRequestDto.fromStationCode))
        toStationCode <- fromEither(TrainDtoMappers.toTrainStationCode(bookTrainItemRequestDto.toStationCode))
        seatClass <- fromEither(TrainDtoMappers.toTrainSeatClass(bookTrainItemRequestDto.seatClass))
        updatedOrder <- trainBookingApplicationService.addTrainItemToOrder(
          actingUserId = currentUserId,
          orderId = OrderId(orderIdValue),
          trainId = TrainId(bookTrainItemRequestDto.trainId),
          travelerIds = bookTrainItemRequestDto.travelerIds.map(TravelerId.apply),
          fromStationCode = fromStationCode,
          toStationCode = toStationCode,
          seatClass = seatClass
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Created(orderResponseDto.asJson)
      yield response
  }

  private def loadRemainingTrainSeats(
      trainJourney: TrainJourney,
      currentTime: java.time.Instant
  ): F[Map[String, Int]] =
    trainJourney.seatInventories.toList
      .traverse { seatInventory =>
        inventoryReservationRepository
          .findReservationsByResource(ReservationResourceType.TrainSeatInventory, seatInventory.inventoryId.value)
          .map { reservations =>
            val reservedQuantity = reservations.foldLeft(0) { (currentQuantity, reservation) =>
              if reservation.reservationStatus == ReservationStatus.Confirmed || reservation.isActiveAt(currentTime) then currentQuantity + reservation.quantity
              else currentQuantity
            }
            seatInventory.inventoryId.value -> (seatInventory.saleableSeats.value - reservedQuantity).max(0)
          }
      }
      .map(_.toMap)
