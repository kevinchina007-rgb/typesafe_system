package com.typesafe.travel.api

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers
import org.http4s.multipart.Multipart

import java.time.{Instant, LocalDate}
import java.nio.file.{Files => NioFiles, Path}

final class ApiRouter[F[_]: Async](
    userService: UserService[F],
    travelerProfileService: TravelerProfileService[F],
    orderService: OrderService[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    avatarApplicationService: AvatarApplicationService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    orderRepository: OrderRepository[F],
    avatarUploadRootDirectoryPath: Path
) extends Http4sDsl[F]:

  import JsonCodecs.given

  private given createUserDecoder: EntityDecoder[F, CreateUserRequestDto] = jsonOf[F, CreateUserRequestDto]
  private given loginUserDecoder: EntityDecoder[F, LoginUserRequestDto] = jsonOf[F, LoginUserRequestDto]
  private given createTravelerDecoder: EntityDecoder[F, CreateTravelerRequestDto] = jsonOf[F, CreateTravelerRequestDto]
  private given createOrderDecoder: EntityDecoder[F, CreateOrderRequestDto] = jsonOf[F, CreateOrderRequestDto]
  private given addFlightItemDecoder: EntityDecoder[F, AddFlightItemRequestDto] = jsonOf[F, AddFlightItemRequestDto]
  private given addHotelItemDecoder: EntityDecoder[F, AddHotelItemRequestDto] = jsonOf[F, AddHotelItemRequestDto]
  private given authorizePaymentDecoder: EntityDecoder[F, AuthorizePaymentRequestDto] = jsonOf[F, AuthorizePaymentRequestDto]
  private given requestRefundDecoder: EntityDecoder[F, RequestRefundRequestDto] = jsonOf[F, RequestRefundRequestDto]
  private given multipartDecoder: EntityDecoder[F, Multipart[F]] = EntityDecoder.multipart[F]

  private val baseRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "uploads" / "avatars" / fileNameValue =>
      val normalizedFileName = fileNameValue.trim
      if normalizedFileName.isEmpty || normalizedFileName.contains("\\") || normalizedFileName.contains("/") then
        NotFound()
      else
        val avatarFilePath = avatarUploadRootDirectoryPath.resolve(normalizedFileName).normalize()
        if avatarFilePath.startsWith(avatarUploadRootDirectoryPath) then
          Async[F].blocking(NioFiles.exists(avatarFilePath)).flatMap {
            case false =>
              NotFound()
            case true =>
              Async[F].blocking(NioFiles.readAllBytes(avatarFilePath)).flatMap { avatarBytes =>
                Ok(avatarBytes).map(
                  _.putHeaders(
                    headers.`Content-Type`(
                      normalizedFileName.toLowerCase match
                        case name if name.endsWith(".png")  => MediaType.image.png
                        case name if name.endsWith(".jpg")  => MediaType.image.jpeg
                        case name if name.endsWith(".jpeg") => MediaType.image.jpeg
                        case _                              => MediaType.application.`octet-stream`
                    )
                  )
                )
              }
          }
        else
          NotFound()

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
        case None            => NotFound(ApiErrorResponseDto("user_not_found", s"User '$userIdValue' was not found").asJson)
      }

    case request @ POST -> Root / "api" / "users" / userIdValue / "avatar" =>
      for
        multipartPayload <- request.as[Multipart[F]]
        avatarPart <- multipartPayload.parts.find(_.name.contains("avatar")).liftTo[F](AvatarApplicationError.AvatarWasMissing)
        avatarFileName <- avatarPart.filename.liftTo[F](AvatarApplicationError.AvatarWasMissing)
        avatarContentType =
          avatarPart.headers
            .get[headers.`Content-Type`]
            .map(contentTypeHeader => s"${contentTypeHeader.mediaType.mainType}/${contentTypeHeader.mediaType.subType}")
            .getOrElse("")
        avatarBytes <- avatarPart.body.compile.to(Array)
        updatedUser <- avatarApplicationService.uploadUserAvatar(
          userId = UserId(userIdValue),
          originalFileName = avatarFileName,
          contentTypeValue = avatarContentType,
          fileBytes = avatarBytes
        )
        response <- Ok(UserResponseDto.fromDomain(updatedUser).asJson)
      yield response

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

    case request @ PUT -> Root / "api" / "users" / userIdValue / "travelers" / travelerIdValue =>
      for
        updateTravelerRequestDto <- request.as[CreateTravelerRequestDto]
        travelerFullName <- fromEither(PersonName.create(updateTravelerRequestDto.fullName))
        travelerDocumentNumber <- fromEither(DocumentNumber.create(updateTravelerRequestDto.documentNumber))
        travelerPhoneNumber <- fromEither(ContactNumber.create(updateTravelerRequestDto.phone))
        travelerBirthDate <- fromEither(BirthDate.create(LocalDate.parse(updateTravelerRequestDto.birthDate), LocalDate.now()))
        travelerPreferences <- fromEither(
          TravelerPreferences.create(
            travelerSeatPreference = TravelerDtoMappers.toSeatPreference(updateTravelerRequestDto.seatPreference),
            travelerMealPreference = TravelerDtoMappers.toMealPreference(updateTravelerRequestDto.mealPreference),
            accessibilityRequestNotes = updateTravelerRequestDto.accessibilityRequestNotes
          )
        )
        travelerEmergencyContact <- createTravelerEmergencyContact(updateTravelerRequestDto)
        updatedTravelerProfile <- travelerProfileService.updateTravelerProfile(
          ownerUserId = UserId(userIdValue),
          travelerId = TravelerId(travelerIdValue),
          travelerFullName = travelerFullName,
          travelerDocumentType = TravelerDtoMappers.toTravelerDocumentType(updateTravelerRequestDto.documentType),
          travelerDocumentNumber = travelerDocumentNumber,
          travelerPhoneNumber = travelerPhoneNumber,
          travelerBirthDate = travelerBirthDate,
          travelerPreferences = travelerPreferences,
          travelerEmergencyContact = travelerEmergencyContact
        )
        response <- Ok(TravelerResponseDto.fromDomain(updatedTravelerProfile).asJson)
      yield response

    case GET -> Root / "api" / "flights" :? DepartureAirportQueryParamMatcher(departureAirportValue) +&
        ArrivalAirportQueryParamMatcher(arrivalAirportValue) +&
        DepartureDateQueryParamMatcher(departureDateValue) =>
      for
        departureAirportQuery <- parseOptionalSearchText(departureAirportValue)
        arrivalAirportQuery <- parseOptionalSearchText(arrivalAirportValue)
        departureDate <- parseOptionalDate(departureDateValue)
        flights <- flightBookingApplicationService.browseFlights(departureAirportQuery, arrivalAirportQuery, departureDate)
        response <- Ok(FlightListResponseDto(flights.map { case (airline, flight) => FlightResponseDto.fromDomain(airline, flight) }).asJson)
      yield response

    case GET -> Root / "api" / "flights" / flightIdValue =>
      flightBookingApplicationService
        .getFlightDetails(FlightId(flightIdValue))
        .flatMap { case (airline, flight) => Ok(FlightResponseDto.fromDomain(airline, flight).asJson) }

    case GET -> Root / "api" / "hotels" :? HotelLocationQueryParamMatcher(locationValue) +&
        CheckInDateQueryParamMatcher(checkInDateValue) +&
        CheckOutDateQueryParamMatcher(checkOutDateValue) =>
      for
        locationQuery <- parseOptionalSearchText(locationValue)
        stayPeriod <- parseOptionalStayPeriod(checkInDateValue, checkOutDateValue)
        hotels <- hotelBookingApplicationService.browseHotels(locationQuery, stayPeriod)
        response <- Ok(HotelListResponseDto(hotels.map(hotel => HotelResponseDto.fromDomain(hotel, stayPeriod))).asJson)
      yield response

    case GET -> Root / "api" / "hotels" / hotelIdValue :? CheckInDateQueryParamMatcher(checkInDateValue) +&
        CheckOutDateQueryParamMatcher(checkOutDateValue) =>
      for
        stayPeriod <- parseOptionalStayPeriod(checkInDateValue, checkOutDateValue)
        hotel <- hotelBookingApplicationService.getHotelDetails(HotelId(hotelIdValue))
        response <- Ok(HotelResponseDto.fromDomain(hotel, stayPeriod).asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" =>
      for
        createOrderRequestDto <- request.as[CreateOrderRequestDto]
        createdOrder <- orderService.createDraftOrder(
          ownerUserId = UserId(createOrderRequestDto.ownerUserId),
          orderCurrency = OrderDtoMappers.toCurrency(createOrderRequestDto.orderCurrency),
          createdAt = Instant.now()
        )
        response <- Created(OrderResponseDto.fromDomain(createdOrder).asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "flight-items" =>
      for
        addFlightItemRequestDto <- request.as[AddFlightItemRequestDto]
        selectedCabinClass <- fromEither(OrderDtoMappers.toCabinClass(addFlightItemRequestDto.cabinClass))
        updatedOrder <- flightBookingApplicationService.addFlightItemToOrder(
          actingUserId = UserId(addFlightItemRequestDto.buyerUserId),
          orderId = OrderId(orderIdValue),
          flightId = FlightId(addFlightItemRequestDto.flightId),
          travelerIds = addFlightItemRequestDto.travelerIds.map(TravelerId.apply),
          cabinClass = selectedCabinClass
        )
        response <- Ok(OrderResponseDto.fromDomain(updatedOrder).asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "hotel-items" =>
      for
        addHotelItemRequestDto <- request.as[AddHotelItemRequestDto]
        roomCount <- fromEither(RoomCount.create(addHotelItemRequestDto.roomCount))
        updatedOrder <- hotelBookingApplicationService.addHotelItemToOrder(
          actingUserId = UserId(addHotelItemRequestDto.buyerUserId),
          orderId = OrderId(orderIdValue),
          roomTypeId = RoomTypeId(addHotelItemRequestDto.roomTypeId),
          guestTravelerIds = addHotelItemRequestDto.guestTravelerIds.map(TravelerId.apply),
          checkInDate = LocalDate.parse(addHotelItemRequestDto.checkInDate),
          checkOutDate = LocalDate.parse(addHotelItemRequestDto.checkOutDate),
          roomCount = roomCount
        )
        response <- Ok(OrderResponseDto.fromDomain(updatedOrder).asJson)
      yield response

    case GET -> Root / "api" / "orders" / orderIdValue =>
      orderRepository.findOrderById(OrderId(orderIdValue)).flatMap {
        case Some(foundOrder) => Ok(OrderResponseDto.fromDomain(foundOrder).asJson)
        case None             => NotFound(ApiErrorResponseDto("order_not_found", s"Order '$orderIdValue' was not found").asJson)
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

  private object DepartureAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("departureAirport")
  private object ArrivalAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("arrivalAirport")
  private object DepartureDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("date")
  private object HotelLocationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("location")
  private object CheckInDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkInDate")
  private object CheckOutDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkOutDate")

  private def parseOptionalSearchText(searchTextValue: Option[String]): F[Option[String]] =
    searchTextValue match
      case Some(value) if value.trim.nonEmpty => MonadThrow[F].pure(Some(value.trim))
      case _                                  => MonadThrow[F].pure(None)

  private def parseOptionalDate(dateValue: Option[String]): F[Option[LocalDate]] =
    dateValue match
      case Some(value) if value.trim.nonEmpty => MonadThrow[F].catchNonFatal(LocalDate.parse(value)).map(Some.apply)
      case _                                  => MonadThrow[F].pure(None)

  private def parseOptionalStayPeriod(
      checkInDateValue: Option[String],
      checkOutDateValue: Option[String]
  ): F[Option[StayPeriod]] =
    (checkInDateValue.map(_.trim).filter(_.nonEmpty), checkOutDateValue.map(_.trim).filter(_.nonEmpty)) match
      case (Some(checkInDateText), Some(checkOutDateText)) =>
        for
          checkInDate <- MonadThrow[F].catchNonFatal(LocalDate.parse(checkInDateText))
          checkOutDate <- MonadThrow[F].catchNonFatal(LocalDate.parse(checkOutDateText))
          stayPeriod <- fromEither(StayPeriod.create(checkInDate, checkOutDate))
        yield Some(stayPeriod)
      case (None, None) =>
        MonadThrow[F].pure(None)
      case _ =>
        MonadThrow[F].raiseError(SharedValidationError.RequiredFieldWasEmpty("stay-period"))

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
    val (responseStatus, apiErrorResponseDto) =
      throwable match
        case UserError.UserEmailAddressAlreadyExists(_) =>
          Status.Conflict -> ApiErrorResponseDto("user_email_exists", throwable.getMessage)
        case UserError.UserWasNotFoundByEmail(_) =>
          Status.NotFound -> ApiErrorResponseDto("user_not_found", throwable.getMessage)
        case UserError.UserWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("user_not_found", throwable.getMessage)
        case FlightError.FlightWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("flight_not_found", throwable.getMessage)
        case FlightBookingApplicationError.CabinWasNotFound(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("cabin_not_found", throwable.getMessage)
        case FlightBookingApplicationError.CabinWasNotBookable(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("cabin_not_bookable", throwable.getMessage)
        case FlightBookingApplicationError.TravelerSelectionWasInvalid(_) =>
          Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", throwable.getMessage)
        case HotelError.HotelWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("hotel_not_found", throwable.getMessage)
        case HotelBookingApplicationError.RoomTypeWasNotFound(_) =>
          Status.BadRequest -> ApiErrorResponseDto("room_type_not_found", throwable.getMessage)
        case HotelBookingApplicationError.StayPeriodWasInvalid(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("stay_period_invalid", throwable.getMessage)
        case HotelBookingApplicationError.RoomCountWasInvalid(_) =>
          Status.BadRequest -> ApiErrorResponseDto("validation_error", throwable.getMessage)
        case HotelBookingApplicationError.RoomInventoryWasNotBookable(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("room_inventory_not_bookable", throwable.getMessage)
        case HotelBookingApplicationError.RoomCapacityWasExceeded(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("room_capacity_exceeded", throwable.getMessage)
        case HotelBookingApplicationError.TravelerSelectionWasInvalid(_) =>
          Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", throwable.getMessage)
        case AvatarApplicationError.AvatarWasMissing =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_missing", throwable.getMessage)
        case AvatarApplicationError.AvatarFileTypeWasInvalid(_) | AvatarApplicationError.AvatarFileExtensionWasInvalid(_) =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_type_invalid", throwable.getMessage)
        case AvatarApplicationError.AvatarFileWasTooLarge(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_too_large", throwable.getMessage)
        case AvatarApplicationError.AvatarUploadFailed(_) =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_upload_failed", throwable.getMessage)
        case TravelerError.TravelerDocumentNumberAlreadyExists(_) =>
          Status.Conflict -> ApiErrorResponseDto("traveler_document_exists", throwable.getMessage)
        case OrderError.OrderCurrencyDidNotMatch(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("currency_mismatch", throwable.getMessage)
        case OrderError.OrderWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("order_not_found", throwable.getMessage)
        case sharedValidationError: SharedValidationError =>
          Status.BadRequest -> ApiErrorResponseDto("validation_error", sharedValidationError.message)
        case _ =>
          Status.BadRequest -> ApiErrorResponseDto("bad_request", throwable.getMessage)

    Response[F](status = responseStatus).withEntity(apiErrorResponseDto.asJson).pure[F]

object ApiRouter:
  def apply[F[_]: Async](
      userService: UserService[F],
      travelerProfileService: TravelerProfileService[F],
      orderService: OrderService[F],
      flightBookingApplicationService: FlightBookingApplicationService[F],
      hotelBookingApplicationService: HotelBookingApplicationService[F],
      avatarApplicationService: AvatarApplicationService[F],
      userRepository: UserRepository[F],
      travelerProfileRepository: TravelerProfileRepository[F],
      orderRepository: OrderRepository[F],
      avatarUploadRootDirectoryPath: Path
  ): ApiRouter[F] =
    new ApiRouter[F](
      userService,
      travelerProfileService,
      orderService,
      flightBookingApplicationService,
      hotelBookingApplicationService,
      avatarApplicationService,
      userRepository,
      travelerProfileRepository,
      orderRepository,
      avatarUploadRootDirectoryPath
    )
