package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.LocalDate

trait AttractionApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  private given EntityDecoder[F, CreateTicketSessionRequestDto] = jsonOf[F, CreateTicketSessionRequestDto]

  protected final def attractionRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "attractions" / "suggestions" :? SearchQueryParamMatcher(queryValue) =>
      for
        queryText <- fromEither(queryValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        suggestions <- attractionBookingApplicationService.suggestAttractions(queryText)
        response <- Ok(SearchSuggestionListResponseDto(suggestions.map(SearchSuggestionResponseDto.fromApplication)).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "managers" =>
      for
        registerAttractionManagerRequestDto <- request.as[RegisterAttractionManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerAttractionManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerAttractionManagerRequestDto.displayName))
        createdAt <- currentInstantF
        adminSession <- attractionAdminApplicationService.registerAttractionManager(primaryEmailAddress, displayName, createdAt)
        _ <- authApplicationService.createManagerCredential(
          managerType = com.typesafe.travel.auth.domain.AuthManagerType.Attraction,
          managerId = adminSession.attractionManager.managerId,
          loginEmail = primaryEmailAddress,
          rawPassword = registerAttractionManagerRequestDto.password,
          now = createdAt
        )
        response <- Created(attractionAdminSessionResponseDto(adminSession).asJson)
      yield response

    case request @ GET -> Root / "api" / "attraction-admin" / "attractions" :? ManagerIdQueryParamMatcher(managerIdValue) =>
      for
        currentManager <- requireCurrentManager(request)
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Attraction && currentManager.managerId == ManagerId(managerIdText) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        attractions <- attractionAdminApplicationService.listManagedAttractions(ManagerId(managerIdText))
        response <- Ok(AttractionListResponseDto(attractions.map(attraction => attractionResponseDto(attraction))).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "attractions" =>
      for
        currentManager <- requireCurrentManager(request)
        createAttractionRequestDto <- request.as[CreateAttractionRequestDto]
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Attraction && currentManager.managerId == ManagerId(createAttractionRequestDto.managerId) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        createdAt <- currentInstantF
        attraction <- attractionAdminApplicationService.createAttraction(
          managerId = ManagerId(createAttractionRequestDto.managerId),
          attractionName = createAttractionRequestDto.attractionName,
          city = createAttractionRequestDto.city,
          location = createAttractionRequestDto.location,
          description = createAttractionRequestDto.description,
          createdAt = createdAt
        )
        response <- Created(attractionResponseDto(attraction).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "ticket-types" =>
      for
        currentManager <- requireCurrentManager(request)
        createTicketTypeRequestDto <- request.as[CreateTicketTypeRequestDto]
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Attraction && currentManager.managerId == ManagerId(createTicketTypeRequestDto.managerId) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        unitPrice <- fromEither(
          Money.create(
            amount = BigDecimal(createTicketTypeRequestDto.unitPrice),
            currency = OrderDtoMappers.toCurrency(createTicketTypeRequestDto.currency)
          )
        )
        validWeekdays <- fromEither(AttractionDtoMappers.toValidWeekdays(createTicketTypeRequestDto.validWeekdays))
        createdAt <- currentInstantF
        attraction <- attractionAdminApplicationService.createTicketType(
          managerId = ManagerId(createTicketTypeRequestDto.managerId),
          attractionId = AttractionId(createTicketTypeRequestDto.attractionId),
          ticketTypeName = createTicketTypeRequestDto.ticketTypeName,
          description = createTicketTypeRequestDto.description,
          unitPrice = unitPrice,
          availableFromDate = LocalDate.parse(createTicketTypeRequestDto.availableFromDate),
          availableToDate = LocalDate.parse(createTicketTypeRequestDto.availableToDate),
          totalQuantity = createTicketTypeRequestDto.totalQuantity,
          validWeekdays = validWeekdays,
          createdAt = createdAt
        )
        response <- Created(attractionResponseDto(attraction).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "ticket-sessions" =>
      for
        currentManager <- requireCurrentManager(request)
        createTicketSessionRequestDto <- request.as[CreateTicketSessionRequestDto]
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Attraction && currentManager.managerId == ManagerId(createTicketSessionRequestDto.managerId) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        createdAt <- currentInstantF
        attraction <- attractionAdminApplicationService.createTicketSession(
          managerId = ManagerId(createTicketSessionRequestDto.managerId),
          attractionId = AttractionId(createTicketSessionRequestDto.attractionId),
          ticketTypeId = TicketTypeId(createTicketSessionRequestDto.ticketTypeId),
          sessionName = createTicketSessionRequestDto.sessionName,
          useDate = LocalDate.parse(createTicketSessionRequestDto.useDate),
          startsAt = java.time.Instant.parse(createTicketSessionRequestDto.startsAt),
          endsAt = java.time.Instant.parse(createTicketSessionRequestDto.endsAt),
          capacity = createTicketSessionRequestDto.capacity,
          createdAt = createdAt
        )
        response <- Created(attractionResponseDto(attraction).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "ticket-types" / "rules" =>
      for
        currentManager <- requireCurrentManager(request)
        createTicketEligibilityRuleRequestDto <- request.as[CreateTicketEligibilityRuleRequestDto]
        _ <- if currentManager.managerType == com.typesafe.travel.auth.domain.AuthManagerType.Attraction && currentManager.managerId == ManagerId(createTicketEligibilityRuleRequestDto.managerId) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        ruleConfig <- fromEither(AttractionDtoMappers.toRuleConfig(createTicketEligibilityRuleRequestDto))
        createdAt <- currentInstantF
        attraction <- attractionAdminApplicationService.addTicketEligibilityRule(
          managerId = ManagerId(createTicketEligibilityRuleRequestDto.managerId),
          attractionId = AttractionId(createTicketEligibilityRuleRequestDto.attractionId),
          ticketTypeId = TicketTypeId(createTicketEligibilityRuleRequestDto.ticketTypeId),
          ruleType = AttractionDtoMappers.toRuleType(createTicketEligibilityRuleRequestDto.ruleType),
          ruleConfig = ruleConfig,
          createdAt = createdAt
        )
        response <- Created(attractionResponseDto(attraction).asJson)
      yield response

    case GET -> Root / "api" / "attractions" :? AttractionCityQueryParamMatcher(cityValue) +& UseDateQueryParamMatcher(useDateValue) =>
      for
        requestedUseDate <- parseOptionalDate(useDateValue)
        attractions <- attractionBookingApplicationService.browseAttractions(cityValue)
        soldQuantities <- loadSoldAttractionQuantities
        response <- Ok(
          AttractionListResponseDto(
            attractions.map(attraction => attractionResponseDto(attraction, requestedUseDate, remainingAttractionQuantities(attraction, requestedUseDate, soldQuantities)))
          ).asJson
        )
      yield response

    case GET -> Root / "api" / "attractions" / attractionIdValue :? UseDateQueryParamMatcher(useDateValue) =>
      for
        requestedUseDate <- parseOptionalDate(useDateValue)
        attraction <- attractionBookingApplicationService.getAttractionDetails(AttractionId(attractionIdValue))
        soldQuantities <- loadSoldAttractionQuantities
        response <- Ok(attractionResponseDto(attraction, requestedUseDate, remainingAttractionQuantities(attraction, requestedUseDate, soldQuantities)).asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "attraction-items" =>
      for
        currentUserId <- requireCurrentUserId(request)
        bookAttractionItemRequestDto <- request.as[BookAttractionItemRequestDto]
        currentTime <- currentInstantF
        updatedOrder <- attractionBookingApplicationService.addAttractionItemToOrder(
          actingUserId = currentUserId,
          orderId = OrderId(orderIdValue),
          attractionId = AttractionId(bookAttractionItemRequestDto.attractionId),
          ticketTypeId = TicketTypeId(bookAttractionItemRequestDto.ticketTypeId),
          sessionId = bookAttractionItemRequestDto.sessionId.map(AttractionTicketSessionId.apply),
          travelerIds = bookAttractionItemRequestDto.travelerIds.map(TravelerId.apply),
          useDate = LocalDate.parse(bookAttractionItemRequestDto.useDate),
          now = currentTime
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response
  }

  private object UseDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("useDate")

  private def loadSoldAttractionQuantities: F[Map[(TicketTypeId, LocalDate), Int]] =
    orderRepository.findAllOrders.map(soldAttractionQuantities)

  private def remainingAttractionQuantities(
      attraction: Attraction,
      requestedUseDate: Option[LocalDate],
      soldQuantities: Map[(TicketTypeId, LocalDate), Int]
  ): Map[TicketTypeId, Int] =
    requestedUseDate match
      case Some(useDate) =>
        attraction.ticketTypes.iterator.map { ticketType =>
          val soldQuantity = soldQuantities.getOrElse((ticketType.ticketTypeId, useDate), 0)
          ticketType.ticketTypeId -> (ticketType.totalQuantity - soldQuantity).max(0)
        }.toMap
      case None =>
        Map.empty

  private def soldAttractionQuantities(orders: List[Order]): Map[(TicketTypeId, LocalDate), Int] =
    orders.iterator
      .filter(order => Set(OrderStatus.Confirmed, OrderStatus.PartiallyRefunded, OrderStatus.Refunded).contains(order.orderStatus))
      .flatMap(_.orderLineItems.iterator)
      .collect {
        case attractionOrderItem: AttractionOrderItem if attractionOrderItem.orderItemStatus != OrderItemStatus.Cancelled =>
          (attractionOrderItem.attractionTicketSnapshot.ticketTypeId, attractionOrderItem.attractionTicketSnapshot.useDate) ->
            attractionOrderItem.attractionTicketSnapshot.travelerIds.size
      }
      .toList
      .groupMapReduce(_._1)(_._2)(_ + _)
