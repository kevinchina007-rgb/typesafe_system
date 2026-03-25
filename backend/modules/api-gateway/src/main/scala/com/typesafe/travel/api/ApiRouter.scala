package com.typesafe.travel.api

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.syntax.all.*
import cats.data.{Kleisli, OptionT}
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.{Instant, LocalDate, OffsetDateTime}

final class ApiRouter[F[_]: Async](
    userService: UserService[F],
    travelerProfileService: TravelerProfileService[F],
    orderService: OrderService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    orderRepository: OrderRepository[F]
) extends Http4sDsl[F]:

  import JsonCodecs.given

  private given createUserDecoder: EntityDecoder[F, CreateUserRequestDto] = jsonOf[F, CreateUserRequestDto]
  private given loginUserDecoder: EntityDecoder[F, LoginUserRequestDto] = jsonOf[F, LoginUserRequestDto]
  private given createTravelerDecoder: EntityDecoder[F, CreateTravelerRequestDto] = jsonOf[F, CreateTravelerRequestDto]
  private given createOrderDecoder: EntityDecoder[F, CreateOrderRequestDto] = jsonOf[F, CreateOrderRequestDto]
  private given authorizePaymentDecoder: EntityDecoder[F, AuthorizePaymentRequestDto] = jsonOf[F, AuthorizePaymentRequestDto]
  private given requestRefundDecoder: EntityDecoder[F, RequestRefundRequestDto] = jsonOf[F, RequestRefundRequestDto]

  private val baseRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "health" =>
      Ok(HealthResponseDto(status = "ok", service = "travel-platform-backend", backendPort = 8080).asJson)

    case request @ POST -> Root / "api" / "users" =>
      for
        createUserRequestDto <- request.as[CreateUserRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(createUserRequestDto.email))
        userDisplayName <- fromEither(PersonName.create(createUserRequestDto.nickname))
        userPhoneNumber <- fromEither(ContactNumber.create(createUserRequestDto.phone))
        createdUser <- userService.registerUser(primaryEmailAddress, userDisplayName, userPhoneNumber, Instant.now())
        response <- Created(UserResponseDto.fromDomain(createdUser).asJson)
      yield response

    case request @ POST -> Root / "api" / "session" / "login" =>
      for
        loginUserRequestDto <- request.as[LoginUserRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(loginUserRequestDto.email))
        signedInUser <- userService.loginUserByEmail(primaryEmailAddress)
        response <- Ok(UserResponseDto.fromDomain(signedInUser).asJson)
      yield response

    case GET -> Root / "api" / "users" / userIdValue =>
      userRepository.findByUserId(UserId(userIdValue)).flatMap {
        case Some(foundUser) => Ok(UserResponseDto.fromDomain(foundUser).asJson)
        case None            => NotFound(ErrorResponseDto(s"User '$userIdValue' was not found").asJson)
      }

    case request @ POST -> Root / "api" / "users" / userIdValue / "travelers" =>
      for
        createTravelerRequestDto <- request.as[CreateTravelerRequestDto]
        travelerFullName <- fromEither(PersonName.create(createTravelerRequestDto.fullName))
        travelerDocumentNumber <- fromEither(DocumentNumber.create(createTravelerRequestDto.documentNumber))
        travelerPhoneNumber <- fromEither(ContactNumber.create(createTravelerRequestDto.phone))
        travelerBirthDate <- fromEither(BirthDate.create(LocalDate.parse(createTravelerRequestDto.birthDate), LocalDate.now()))
        travelerPreferences <- fromEither(
          TravelerPreferences.create(
            travelerSeatPreference = TravelerDtoMappers.toSeatPreference(createTravelerRequestDto.seatPreference),
            travelerMealPreference = TravelerDtoMappers.toMealPreference(createTravelerRequestDto.mealPreference),
            accessibilityRequestNotes = createTravelerRequestDto.accessibilityRequestNotes
          )
        )
        travelerEmergencyContact <- createTravelerEmergencyContact(createTravelerRequestDto)
        createdTravelerProfile <- travelerProfileService.createTravelerProfile(
          ownerUserId = UserId(userIdValue),
          travelerFullName = travelerFullName,
          travelerDocumentType = TravelerDtoMappers.toTravelerDocumentType(createTravelerRequestDto.documentType),
          travelerDocumentNumber = travelerDocumentNumber,
          travelerPhoneNumber = travelerPhoneNumber,
          travelerBirthDate = travelerBirthDate,
          travelerPreferences = travelerPreferences,
          travelerEmergencyContact = travelerEmergencyContact,
          requestedDefaultTravelerProfile = createTravelerRequestDto.isDefaultTraveler
        )
        response <- Created(TravelerResponseDto.fromDomain(createdTravelerProfile).asJson)
      yield response

    case GET -> Root / "api" / "users" / userIdValue / "travelers" =>
      travelerProfileRepository
        .findTravelerProfilesByOwnerUserId(UserId(userIdValue))
        .flatMap(travelers => Ok(TravelerListResponseDto(travelers.map(TravelerResponseDto.fromDomain)).asJson))

    case request @ POST -> Root / "api" / "orders" =>
      for
        createOrderRequestDto <- request.as[CreateOrderRequestDto]
        createdOrder <- orderService.createDraftOrder(
          ownerUserId = UserId(createOrderRequestDto.ownerUserId),
          orderCurrency = OrderDtoMappers.toCurrency(createOrderRequestDto.orderCurrency),
          createdAt = Instant.now()
        )
        populatedOrder <- addInitialOrderItem(createdOrder.orderId, createOrderRequestDto)
        response <- Created(OrderResponseDto.fromDomain(populatedOrder).asJson)
      yield response

    case GET -> Root / "api" / "orders" / orderIdValue =>
      orderRepository.findOrderById(OrderId(orderIdValue)).flatMap {
        case Some(foundOrder) => Ok(OrderResponseDto.fromDomain(foundOrder).asJson)
        case None             => NotFound(ErrorResponseDto(s"Order '$orderIdValue' was not found").asJson)
      }

    case POST -> Root / "api" / "orders" / orderIdValue / "submit" =>
      orderService
        .submitOrderForPayment(OrderId(orderIdValue))
        .flatMap(order => Ok(OrderResponseDto.fromDomain(order).asJson))

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "payments" =>
      for
        authorizePaymentRequestDto <- request.as[AuthorizePaymentRequestDto]
        paymentAmount <- fromEither(
          Money.create(
            amount = BigDecimal(authorizePaymentRequestDto.paymentAmount),
            currency = OrderDtoMappers.toCurrency(authorizePaymentRequestDto.paymentCurrency)
          )
        )
        orderAfterPaymentAuthorization <- orderService.authorizeOrderPayment(
          orderId = OrderId(orderIdValue),
          paymentAmount = paymentAmount,
          paymentMethod = OrderDtoMappers.toPaymentMethod(authorizePaymentRequestDto.paymentMethod),
          authorizedAt = Instant.now()
        )
        response <- Ok(OrderResponseDto.fromDomain(orderAfterPaymentAuthorization).asJson)
      yield response

    case POST -> Root / "api" / "orders" / orderIdValue / "payments" / paymentIdValue / "capture" =>
      orderService
        .captureAuthorizedPayment(OrderId(orderIdValue), PaymentId(paymentIdValue), Instant.now())
        .flatMap(order => Ok(OrderResponseDto.fromDomain(order).asJson))

    case POST -> Root / "api" / "orders" / orderIdValue / "cancel" =>
      orderRepository
        .findOrderById(OrderId(orderIdValue))
        .flatMap(_.liftTo[F](OrderError.OrderWasNotFound(OrderId(orderIdValue))))
        .flatMap(_.cancelDraftOrder(Instant.now()).liftTo[F])
        .flatMap(orderRepository.saveOrder)
        .flatMap(order => Ok(OrderResponseDto.fromDomain(order).asJson))

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "refunds" =>
      for
        requestRefundRequestDto <- request.as[RequestRefundRequestDto]
        refundAmount <- fromEither(
          Money.create(
            amount = BigDecimal(requestRefundRequestDto.refundAmount),
            currency = OrderDtoMappers.toCurrency(requestRefundRequestDto.refundCurrency)
          )
        )
        updatedOrder <- orderService.requestOrderRefund(
          orderId = OrderId(orderIdValue),
          refundAmount = refundAmount,
          refundReason = requestRefundRequestDto.refundReason,
          requestedAt = Instant.now()
        )
        response <- Ok(OrderResponseDto.fromDomain(updatedOrder).asJson)
      yield response

    case POST -> Root / "api" / "orders" / orderIdValue / "refunds" / refundIdValue / "approve" =>
      orderService
        .approveRequestedRefund(OrderId(orderIdValue), RefundId(refundIdValue), Instant.now())
        .flatMap(order => Ok(OrderResponseDto.fromDomain(order).asJson))

    case POST -> Root / "api" / "orders" / orderIdValue / "refunds" / refundIdValue / "settle" =>
      orderService
        .settleApprovedRefund(OrderId(orderIdValue), RefundId(refundIdValue), Instant.now())
        .flatMap(order => Ok(OrderResponseDto.fromDomain(order).asJson))
  }

  val routes: HttpRoutes[F] =
    Kleisli { request =>
      baseRoutes.run(request).handleErrorWith(throwable => OptionT.liftF(handleDomainError(throwable)))
    }

  private def addInitialOrderItem(orderId: OrderId, createOrderRequestDto: CreateOrderRequestDto): F[Order] =
    val bookedMoney = Money.create(BigDecimal(createOrderRequestDto.bookedAmount), OrderDtoMappers.toCurrency(createOrderRequestDto.orderCurrency))
    createOrderRequestDto.itemKind match
      case "flight" =>
        for
          orderLineMoney <- fromEither(bookedMoney)
          flightNumber <- fromEither(FlightNumber.create(createOrderRequestDto.referenceCode))
          departureAirportCode <- fromEither(AirportCode.create(createOrderRequestDto.originCode))
          arrivalAirportCode <- fromEither(AirportCode.create(createOrderRequestDto.destinationCode))
          cabinCode <- fromEither(CabinCode.create(createOrderRequestDto.variantLabel))
          flightSchedule <- fromEither(
            FlightSchedule.create(
              departureAt = OffsetDateTime.parse(createOrderRequestDto.periodStart),
              arrivalAt = OffsetDateTime.parse(createOrderRequestDto.periodEnd)
            )
          )
          order <- orderService.addFlightOrderItem(
            orderId = orderId,
            flightBookingSnapshot = FlightBookingSnapshot(
              airlineId = AirlineId(createOrderRequestDto.providerId),
              flightId = FlightId(createOrderRequestDto.productId),
              flightNumber = flightNumber,
              flightSchedule = flightSchedule,
              departureAirportCode = departureAirportCode,
              arrivalAirportCode = arrivalAirportCode,
              cabinCode = cabinCode,
              travelerId = TravelerId(createOrderRequestDto.travelerId)
            ),
            bookedMoney = orderLineMoney
          )
        yield order
      case _ =>
        for
          orderLineMoney <- fromEither(bookedMoney)
          hotelName <- fromEither(HotelName.create(createOrderRequestDto.providerLabel))
          roomTypeName <- fromEither(RoomTypeName.create(createOrderRequestDto.variantLabel))
          stayPeriod <- fromEither(
            StayPeriod.create(
              checkIn = LocalDate.parse(createOrderRequestDto.periodStart),
              checkOut = LocalDate.parse(createOrderRequestDto.periodEnd)
            )
          )
          guestCount <- fromEither(Capacity.create(createOrderRequestDto.quantity))
          order <- orderService.addHotelOrderItem(
            orderId = orderId,
            hotelBookingSnapshot = HotelBookingSnapshot(
              hotelId = HotelId(createOrderRequestDto.providerId),
              hotelName = hotelName,
              roomTypeId = RoomTypeId(createOrderRequestDto.productId),
              roomTypeName = roomTypeName,
              stayPeriod = stayPeriod,
              guestCount = guestCount
            ),
            bookedMoney = orderLineMoney
          )
        yield order

  private def createTravelerEmergencyContact(
      createTravelerRequestDto: CreateTravelerRequestDto
  ): F[Option[TravelerEmergencyContact]] =
    (createTravelerRequestDto.emergencyContactName, createTravelerRequestDto.emergencyContactPhoneNumber) match
      case (Some(contactName), Some(contactPhoneNumber)) =>
        for
          parsedContactName <- fromEither(PersonName.create(contactName))
          parsedContactNumber <- fromEither(ContactNumber.create(contactPhoneNumber))
        yield Some(TravelerEmergencyContact.create(parsedContactName, parsedContactNumber))
      case _ =>
        MonadThrow[F].pure(None)

  private def fromEither[A](value: Either[? <: Throwable, A]): F[A] =
    MonadThrow[F].fromEither(value.leftMap(identity))

  private def handleDomainError(throwable: Throwable): F[Response[F]] =
    BadRequest(ErrorResponseDto(throwable.getMessage).asJson)

object ApiRouter:
  def apply[F[_]: Async](
      userService: UserService[F],
      travelerProfileService: TravelerProfileService[F],
      orderService: OrderService[F],
      userRepository: UserRepository[F],
      travelerProfileRepository: TravelerProfileRepository[F],
      orderRepository: OrderRepository[F]
  ): ApiRouter[F] =
    new ApiRouter[F](userService, travelerProfileService, orderService, userRepository, travelerProfileRepository, orderRepository)
