package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.LocalDate

trait AttractionApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def attractionRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "attraction-admin" / "managers" =>
      for
        registerAttractionManagerRequestDto <- request.as[RegisterAttractionManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerAttractionManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerAttractionManagerRequestDto.displayName))
        createdAt <- currentInstantF
        adminSession <- attractionAdminApplicationService.registerAttractionManager(primaryEmailAddress, displayName, createdAt)
        response <- Created(AttractionAdminSessionResponseDto.fromApplication(adminSession).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "session" / "login" =>
      for
        attractionAdminLoginRequestDto <- request.as[AttractionAdminLoginRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(attractionAdminLoginRequestDto.email))
        adminSession <- attractionAdminApplicationService.loginAttractionManager(primaryEmailAddress)
        response <- Ok(AttractionAdminSessionResponseDto.fromApplication(adminSession).asJson)
      yield response

    case GET -> Root / "api" / "attraction-admin" / "attractions" :? ManagerIdQueryParamMatcher(managerIdValue) =>
      for
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        attractions <- attractionAdminApplicationService.listManagedAttractions(ManagerId(managerIdText))
        response <- Ok(AttractionListResponseDto(attractions.map(AttractionResponseDto.fromDomain)).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "attractions" =>
      for
        createAttractionRequestDto <- request.as[CreateAttractionRequestDto]
        createdAt <- currentInstantF
        attraction <- attractionAdminApplicationService.createAttraction(
          managerId = ManagerId(createAttractionRequestDto.managerId),
          attractionName = createAttractionRequestDto.attractionName,
          city = createAttractionRequestDto.city,
          location = createAttractionRequestDto.location,
          description = createAttractionRequestDto.description,
          createdAt = createdAt
        )
        response <- Created(AttractionResponseDto.fromDomain(attraction).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "ticket-types" =>
      for
        createTicketTypeRequestDto <- request.as[CreateTicketTypeRequestDto]
        unitPrice <- fromEither(
          Money.create(
            amount = BigDecimal(createTicketTypeRequestDto.unitPrice),
            currency = OrderDtoMappers.toCurrency(createTicketTypeRequestDto.currency)
          )
        )
        createdAt <- currentInstantF
        attraction <- attractionAdminApplicationService.createTicketType(
          managerId = ManagerId(createTicketTypeRequestDto.managerId),
          attractionId = AttractionId(createTicketTypeRequestDto.attractionId),
          ticketTypeName = createTicketTypeRequestDto.ticketTypeName,
          description = createTicketTypeRequestDto.description,
          unitPrice = unitPrice,
          createdAt = createdAt
        )
        response <- Created(AttractionResponseDto.fromDomain(attraction).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "ticket-types" / "rules" =>
      for
        createTicketEligibilityRuleRequestDto <- request.as[CreateTicketEligibilityRuleRequestDto]
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
        response <- Created(AttractionResponseDto.fromDomain(attraction).asJson)
      yield response

    case GET -> Root / "api" / "attractions" :? AttractionCityQueryParamMatcher(cityValue) =>
      attractionBookingApplicationService
        .browseAttractions(cityValue)
        .flatMap(attractions => Ok(AttractionListResponseDto(attractions.map(AttractionResponseDto.fromDomain)).asJson))

    case GET -> Root / "api" / "attractions" / attractionIdValue =>
      attractionBookingApplicationService
        .getAttractionDetails(AttractionId(attractionIdValue))
        .flatMap(attraction => Ok(AttractionResponseDto.fromDomain(attraction).asJson))

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "attraction-items" =>
      for
        bookAttractionItemRequestDto <- request.as[BookAttractionItemRequestDto]
        currentTime <- currentInstantF
        updatedOrder <- attractionBookingApplicationService.addAttractionItemToOrder(
          actingUserId = UserId(bookAttractionItemRequestDto.buyerUserId),
          orderId = OrderId(orderIdValue),
          attractionId = AttractionId(bookAttractionItemRequestDto.attractionId),
          ticketTypeId = TicketTypeId(bookAttractionItemRequestDto.ticketTypeId),
          travelerIds = bookAttractionItemRequestDto.travelerIds.map(TravelerId.apply),
          useDate = LocalDate.parse(bookAttractionItemRequestDto.useDate),
          now = currentTime
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response
  }
