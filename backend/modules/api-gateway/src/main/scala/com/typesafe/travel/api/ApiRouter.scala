package com.typesafe.travel.api

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.traveler.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers
import org.http4s.multipart.Multipart

import java.time.{Instant, LocalDate, OffsetDateTime}
import java.nio.file.{Files => NioFiles, Path}

final class ApiRouter[F[_]: Async](
    userService: UserService[F],
    travelerProfileService: TravelerProfileService[F],
    orderService: OrderService[F],
    orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    trainBookingApplicationService: TrainBookingApplicationService[F],
    trainAdminApplicationService: TrainAdminApplicationService[F],
    attractionBookingApplicationService: AttractionBookingApplicationService[F],
    attractionAdminApplicationService: AttractionAdminApplicationService[F],
    managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
    avatarApplicationService: AvatarApplicationService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    orderRepository: OrderRepository[F],
    inventoryReservationRepository: InventoryReservationRepository[F],
    avatarUploadRootDirectoryPath: Path,
    frontendDistRootDirectoryPath: Path
) extends Http4sDsl[F]:

  import JsonCodecs.given

  private given createUserDecoder: EntityDecoder[F, CreateUserRequestDto] = jsonOf[F, CreateUserRequestDto]
  private given loginUserDecoder: EntityDecoder[F, LoginUserRequestDto] = jsonOf[F, LoginUserRequestDto]
  private given createTravelerDecoder: EntityDecoder[F, CreateTravelerRequestDto] = jsonOf[F, CreateTravelerRequestDto]
  private given createOrderDecoder: EntityDecoder[F, CreateOrderRequestDto] = jsonOf[F, CreateOrderRequestDto]
  private given managerLoginDecoder: EntityDecoder[F, ManagerLoginRequestDto] = jsonOf[F, ManagerLoginRequestDto]
  private given managerDecisionDecoder: EntityDecoder[F, ManagerDecisionRequestDto] = jsonOf[F, ManagerDecisionRequestDto]
  private given registerAirlineManagerDecoder: EntityDecoder[F, RegisterAirlineManagerRequestDto] = jsonOf[F, RegisterAirlineManagerRequestDto]
  private given registerHotelManagerDecoder: EntityDecoder[F, RegisterHotelManagerRequestDto] = jsonOf[F, RegisterHotelManagerRequestDto]
  private given createManagerFlightDecoder: EntityDecoder[F, CreateManagerFlightRequestDto] = jsonOf[F, CreateManagerFlightRequestDto]
  private given createManagerRoomTypeDecoder: EntityDecoder[F, CreateManagerRoomTypeRequestDto] = jsonOf[F, CreateManagerRoomTypeRequestDto]
  private given registerRailwayManagerDecoder: EntityDecoder[F, RegisterRailwayManagerRequestDto] = jsonOf[F, RegisterRailwayManagerRequestDto]
  private given trainAdminLoginDecoder: EntityDecoder[F, TrainAdminLoginRequestDto] = jsonOf[F, TrainAdminLoginRequestDto]
  private given createTrainJourneyDecoder: EntityDecoder[F, CreateTrainJourneyRequestDto] = jsonOf[F, CreateTrainJourneyRequestDto]
  private given registerAttractionManagerDecoder: EntityDecoder[F, RegisterAttractionManagerRequestDto] = jsonOf[F, RegisterAttractionManagerRequestDto]
  private given attractionAdminLoginDecoder: EntityDecoder[F, AttractionAdminLoginRequestDto] = jsonOf[F, AttractionAdminLoginRequestDto]
  private given createAttractionDecoder: EntityDecoder[F, CreateAttractionRequestDto] = jsonOf[F, CreateAttractionRequestDto]
  private given createTicketTypeDecoder: EntityDecoder[F, CreateTicketTypeRequestDto] = jsonOf[F, CreateTicketTypeRequestDto]
  private given createTicketEligibilityRuleDecoder: EntityDecoder[F, CreateTicketEligibilityRuleRequestDto] = jsonOf[F, CreateTicketEligibilityRuleRequestDto]
  private given addAttractionItemDecoder: EntityDecoder[F, BookAttractionItemRequestDto] = jsonOf[F, BookAttractionItemRequestDto]
  private given addFlightItemDecoder: EntityDecoder[F, BookFlightRequestDto] = jsonOf[F, BookFlightRequestDto]
  private given addHotelItemDecoder: EntityDecoder[F, BookHotelRequestDto] = jsonOf[F, BookHotelRequestDto]
  private given addTrainItemDecoder: EntityDecoder[F, BookTrainItemRequestDto] = jsonOf[F, BookTrainItemRequestDto]
  private given authorizePaymentDecoder: EntityDecoder[F, PayOrderRequestDto] = jsonOf[F, PayOrderRequestDto]
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
      Ok(
        HealthResponseDto(
          status = "ok",
          service = "travel-platform-backend",
          backendPort = sys.env.get("TRAVEL_BACKEND_PORT").flatMap(_.trim.toIntOption).getOrElse(19095)
        ).asJson
      )

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

    case request @ POST -> Root / "api" / "manager" / "session" / "login" =>
      for
        managerLoginRequestDto <- request.as[ManagerLoginRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(managerLoginRequestDto.email))
        managerSession <- managerWorkflowApplicationService.loginManager(
          managerType = ManagerDtoMappers.toManagerType(managerLoginRequestDto.managerType),
          primaryEmailAddress = primaryEmailAddress
        )
        response <- Ok(ManagerSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "airline" / "register" =>
      for
        registerAirlineManagerRequestDto <- request.as[RegisterAirlineManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerAirlineManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerAirlineManagerRequestDto.displayName))
        airlineName <- fromEither(AirlineName.create(registerAirlineManagerRequestDto.airlineName))
        airlineCode <- fromEither(AirlineCode.create(registerAirlineManagerRequestDto.airlineCode))
        managerSession <- managerWorkflowApplicationService.registerAirlineManager(
          primaryEmailAddress = primaryEmailAddress,
          displayName = displayName,
          airlineName = airlineName,
          airlineCode = airlineCode,
          createdAt = Instant.now()
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
        managerSession <- managerWorkflowApplicationService.registerHotelManager(
          primaryEmailAddress = primaryEmailAddress,
          displayName = displayName,
          hotelName = hotelName,
          hotelLocation = hotelLocation,
          createdAt = Instant.now()
        )
        response <- Created(ManagerSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

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

    case DELETE -> Root / "api" / "users" / userIdValue / "travelers" / travelerIdValue =>
      travelerProfileService
        .deleteTravelerProfile(UserId(userIdValue), TravelerId(travelerIdValue))
        .flatMap(_ => NoContent())

    case GET -> Root / "api" / "users" / userIdValue / "orders" =>
      orderLifecycleApplicationService
        .listOrdersForUser(UserId(userIdValue), Instant.now())
        .flatMap(_.traverse(toOrderResponseDto))
        .flatMap(orderResponses => Ok(OrderListResponseDto(orderResponses).asJson))

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

    case request @ POST -> Root / "api" / "train-admin" / "managers" =>
      for
        registerRailwayManagerRequestDto <- request.as[RegisterRailwayManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerRailwayManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerRailwayManagerRequestDto.displayName))
        managerSession <- trainAdminApplicationService.registerRailwayManager(
          operatorCode = registerRailwayManagerRequestDto.operatorCode,
          primaryEmailAddress = primaryEmailAddress,
          displayName = displayName,
          createdAt = Instant.now()
        )
        response <- Created(TrainAdminSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

    case request @ POST -> Root / "api" / "train-admin" / "session" / "login" =>
      for
        trainAdminLoginRequestDto <- request.as[TrainAdminLoginRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(trainAdminLoginRequestDto.email))
        managerSession <- trainAdminApplicationService.loginRailwayManager(primaryEmailAddress)
        response <- Ok(TrainAdminSessionResponseDto.fromApplication(managerSession).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "managers" =>
      for
        registerAttractionManagerRequestDto <- request.as[RegisterAttractionManagerRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(registerAttractionManagerRequestDto.email))
        displayName <- fromEither(PersonName.create(registerAttractionManagerRequestDto.displayName))
        adminSession <- attractionAdminApplicationService.registerAttractionManager(primaryEmailAddress, displayName, Instant.now())
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
        attraction <- attractionAdminApplicationService.createAttraction(
          managerId = ManagerId(createAttractionRequestDto.managerId),
          attractionName = createAttractionRequestDto.attractionName,
          city = createAttractionRequestDto.city,
          location = createAttractionRequestDto.location,
          description = createAttractionRequestDto.description,
          createdAt = Instant.now()
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
        attraction <- attractionAdminApplicationService.createTicketType(
          managerId = ManagerId(createTicketTypeRequestDto.managerId),
          attractionId = AttractionId(createTicketTypeRequestDto.attractionId),
          ticketTypeName = createTicketTypeRequestDto.ticketTypeName,
          description = createTicketTypeRequestDto.description,
          unitPrice = unitPrice,
          createdAt = Instant.now()
        )
        response <- Created(AttractionResponseDto.fromDomain(attraction).asJson)
      yield response

    case request @ POST -> Root / "api" / "attraction-admin" / "ticket-types" / "rules" =>
      for
        createTicketEligibilityRuleRequestDto <- request.as[CreateTicketEligibilityRuleRequestDto]
        ruleConfig <- fromEither(AttractionDtoMappers.toRuleConfig(createTicketEligibilityRuleRequestDto))
        attraction <- attractionAdminApplicationService.addTicketEligibilityRule(
          managerId = ManagerId(createTicketEligibilityRuleRequestDto.managerId),
          attractionId = AttractionId(createTicketEligibilityRuleRequestDto.attractionId),
          ticketTypeId = TicketTypeId(createTicketEligibilityRuleRequestDto.ticketTypeId),
          ruleType = AttractionDtoMappers.toRuleType(createTicketEligibilityRuleRequestDto.ruleType),
          ruleConfig = ruleConfig,
          createdAt = Instant.now()
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
        updatedOrder <- attractionBookingApplicationService.addAttractionItemToOrder(
          actingUserId = UserId(bookAttractionItemRequestDto.buyerUserId),
          orderId = OrderId(orderIdValue),
          attractionId = AttractionId(bookAttractionItemRequestDto.attractionId),
          ticketTypeId = TicketTypeId(bookAttractionItemRequestDto.ticketTypeId),
          travelerIds = bookAttractionItemRequestDto.travelerIds.map(TravelerId.apply),
          useDate = LocalDate.parse(bookAttractionItemRequestDto.useDate),
          now = Instant.now()
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case GET -> Root / "api" / "train-admin" / "trains" :? ManagerIdQueryParamMatcher(managerIdValue) =>
      for
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        trains <- trainAdminApplicationService.listManagedTrains(ManagerId(managerIdText))
        response <- Ok(TrainListResponseDto(trains.map(TrainResponseDto.fromDomain)).asJson)
      yield response

    case request @ POST -> Root / "api" / "train-admin" / "trains" =>
      for
        createTrainJourneyRequestDto <- request.as[CreateTrainJourneyRequestDto]
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
        trainJourney <- trainAdminApplicationService.createTrainJourney(
          managerId = ManagerId(createTrainJourneyRequestDto.managerId),
          trainNumber = trainNumber,
          saleStartsAt = Instant.parse(createTrainJourneyRequestDto.saleStartsAt),
          stops = stops,
          seatConfigs = seatInventories,
          segmentPrices = segmentPrices,
          refundPolicies = refundPolicies,
          createdAt = Instant.now()
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
        response <- Ok(TrainListResponseDto(trains.map(TrainResponseDto.fromDomain)).asJson)
      yield response

    case GET -> Root / "api" / "trains" / trainIdValue =>
      trainBookingApplicationService
        .getTrainDetails(TrainId(trainIdValue))
        .flatMap(trainJourney => Ok(TrainResponseDto.fromDomain(trainJourney).asJson))

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

    case request @ POST -> Root / "api" / "flights" / "book" =>
      for
        addFlightItemRequestDto <- request.as[BookFlightRequestDto]
        selectedCabinClass <- fromEither(OrderDtoMappers.toCabinClass(addFlightItemRequestDto.cabinClass))
        updatedOrder <- flightBookingApplicationService.createFlightOrder(
          actingUserId = UserId(addFlightItemRequestDto.buyerUserId),
          flightId = FlightId(addFlightItemRequestDto.flightId),
          travelerIds = addFlightItemRequestDto.travelerIds.map(TravelerId.apply),
          cabinClass = selectedCabinClass
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Created(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "hotels" / "book" =>
      for
        addHotelItemRequestDto <- request.as[BookHotelRequestDto]
        roomCount <- fromEither(RoomCount.create(addHotelItemRequestDto.roomCount))
        updatedOrder <- hotelBookingApplicationService.createHotelOrder(
          actingUserId = UserId(addHotelItemRequestDto.buyerUserId),
          roomTypeId = RoomTypeId(addHotelItemRequestDto.roomTypeId),
          guestTravelerIds = addHotelItemRequestDto.guestTravelerIds.map(TravelerId.apply),
          checkInDate = LocalDate.parse(addHotelItemRequestDto.checkInDate),
          checkOutDate = LocalDate.parse(addHotelItemRequestDto.checkOutDate),
          roomCount = roomCount
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Created(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "train-items" =>
      for
        bookTrainItemRequestDto <- request.as[BookTrainItemRequestDto]
        fromStationCode <- fromEither(TrainDtoMappers.toTrainStationCode(bookTrainItemRequestDto.fromStationCode))
        toStationCode <- fromEither(TrainDtoMappers.toTrainStationCode(bookTrainItemRequestDto.toStationCode))
        seatClass <- fromEither(TrainDtoMappers.toTrainSeatClass(bookTrainItemRequestDto.seatClass))
        updatedOrder <- trainBookingApplicationService.addTrainItemToOrder(
          actingUserId = UserId(bookTrainItemRequestDto.buyerUserId),
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

    case GET -> Root / "api" / "orders" / orderIdValue =>
      orderLifecycleApplicationService.getOrder(OrderId(orderIdValue), Instant.now()).flatMap { foundOrder =>
        toOrderResponseDto(foundOrder).flatMap(orderResponseDto => Ok(orderResponseDto.asJson))
      }.handleErrorWith {
        case throwable: Throwable => handleDomainError(throwable)
      }

    case GET -> Root / "api" / "manager" / "tasks" :? ManagerIdQueryParamMatcher(managerIdValue) +&
        ManagerTypeQueryParamMatcher(managerTypeValue) +&
        TaskStatusQueryParamMatcher(taskStatusValue) =>
      for
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(managerTypeValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        tasks <- managerWorkflowApplicationService.listManagerTasks(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText),
          requestedSupplierReviewStatuses = parseRequestedSupplierReviewStatuses(taskStatusValue)
        )
        response <- Ok(ManagerBookingTaskListResponseDto(tasks.map(ManagerBookingTaskResponseDto.fromApplication)).asJson)
      yield response

    case GET -> Root / "api" / "manager" / "refund-tasks" :? ManagerIdQueryParamMatcher(managerIdValue) +&
        ManagerTypeQueryParamMatcher(managerTypeValue) =>
      for
        managerIdText <- fromEither(managerIdValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(managerTypeValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        tasks <- managerWorkflowApplicationService.listManagerRefundTasks(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText)
        )
        response <- Ok(ManagerRefundTaskListResponseDto(tasks.map(ManagerRefundTaskResponseDto.fromApplication)).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "flights" =>
      for
        createManagerFlightRequestDto <- request.as[CreateManagerFlightRequestDto]
        flightNumber <- fromEither(FlightNumber.create(createManagerFlightRequestDto.flightNumber))
        departureAirport <- fromEither(AirportCode.create(createManagerFlightRequestDto.departureAirport))
        arrivalAirport <- fromEither(AirportCode.create(createManagerFlightRequestDto.arrivalAirport))
        economySeatCount <- fromEither(SeatCount.create(createManagerFlightRequestDto.economySeatCount))
        economyPrice <- fromEither(Money.create(BigDecimal(createManagerFlightRequestDto.economyPrice), OrderDtoMappers.toCurrency(createManagerFlightRequestDto.currency)))
        businessSeatCount <- fromEither(SeatCount.create(createManagerFlightRequestDto.businessSeatCount))
        businessPrice <- fromEither(Money.create(BigDecimal(createManagerFlightRequestDto.businessPrice), OrderDtoMappers.toCurrency(createManagerFlightRequestDto.currency)))
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
          createdAt = Instant.now()
        )
        response <- Created(FlightResponseDto.fromDomain(airlineAndFlight._1, airlineAndFlight._2).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "hotel-room-types" =>
      for
        createManagerRoomTypeRequestDto <- request.as[CreateManagerRoomTypeRequestDto]
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
        managerDecisionRequestDto <- request.as[ManagerDecisionRequestDto]
        updatedOrder <- managerWorkflowApplicationService.confirmBookingItem(
          managerId = ManagerId(managerDecisionRequestDto.managerId),
          managerType = ManagerDtoMappers.toManagerType(managerDecisionRequestDto.managerType),
          orderItemId = OrderItemId(orderItemValue),
          note = managerDecisionRequestDto.note.map(_.trim).filter(_.nonEmpty),
          decidedAt = Instant.now()
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "booking-items" / orderItemValue / "reject" =>
      for
        managerDecisionRequestDto <- request.as[ManagerDecisionRequestDto]
        rejectReason <- fromEither(
          managerDecisionRequestDto.reason.map(_.trim).filter(_.nonEmpty).toRight(OrderError.SupplierRejectReasonWasEmpty(OrderItemId(orderItemValue)))
        )
        updatedOrder <- managerWorkflowApplicationService.rejectBookingItem(
          managerId = ManagerId(managerDecisionRequestDto.managerId),
          managerType = ManagerDtoMappers.toManagerType(managerDecisionRequestDto.managerType),
          orderItemId = OrderItemId(orderItemValue),
          reason = rejectReason,
          decidedAt = Instant.now()
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "orders" / orderIdValue / "refund" / "approve" =>
      for
        managerIdText <- fromEither(request.params.get("managerId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(request.params.get("managerType").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        order <- managerWorkflowApplicationService.approveRefund(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText),
          orderId = OrderId(orderIdValue),
          decidedAt = Instant.now()
        )
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "manager" / "orders" / orderIdValue / "refund" / "reject" =>
      for
        managerIdText <- fromEither(request.params.get("managerId").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerId")))
        managerTypeText <- fromEither(request.params.get("managerType").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("managerType")))
        order <- managerWorkflowApplicationService.rejectRefund(
          managerId = ManagerId(managerIdText),
          managerType = ManagerDtoMappers.toManagerType(managerTypeText),
          orderId = OrderId(orderIdValue)
        )
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ GET -> path =>
      serveFrontendAsset(path.renderString)

    case POST -> Root / "api" / "orders" / orderIdValue / "submit" =>
      orderService
        .submitOrderForPayment(OrderId(orderIdValue))
        .flatMap(order => toOrderResponseDto(order).flatMap(orderResponseDto => Ok(orderResponseDto.asJson)))

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "pay" =>
      for
        authorizePaymentRequestDto <- request.as[PayOrderRequestDto]
        orderAfterPaymentAuthorization <- orderLifecycleApplicationService.payOrder(
          orderId = OrderId(orderIdValue),
          paymentMethod = OrderDtoMappers.toPaymentMethod(authorizePaymentRequestDto.paymentMethod),
          paymentSucceeded = authorizePaymentRequestDto.paymentSucceeded,
          currentTime = Instant.now()
        )
        orderResponseDto <- toOrderResponseDto(orderAfterPaymentAuthorization)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case POST -> Root / "api" / "orders" / orderIdValue / "cancel" =>
      orderLifecycleApplicationService
        .cancelOrder(OrderId(orderIdValue), Instant.now())
        .flatMap(order => toOrderResponseDto(order).flatMap(orderResponseDto => Ok(orderResponseDto.asJson)))

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "refunds" =>
      for
        requestRefundRequestDto <- request.as[RequestRefundRequestDto]
        existingOrder <- orderRepository.findOrderById(OrderId(orderIdValue)).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(OrderId(orderIdValue))))
        updatedOrder <- if existingOrder.orderLineItems.exists(_.isInstanceOf[TrainOrderItem]) then
          for
            refundAmount <- trainBookingApplicationService.calculateRefundAmountForOrder(OrderId(orderIdValue), Instant.now())
            order <- orderService.requestOrderRefund(
              orderId = OrderId(orderIdValue),
              refundAmount = refundAmount,
              refundReason = requestRefundRequestDto.refundReason,
              requestedAt = Instant.now()
            )
          yield order
        else
          orderService.requestCustomerRefund(
            orderId = OrderId(orderIdValue),
            refundReason = requestRefundRequestDto.refundReason,
            requestedAt = Instant.now()
          )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case POST -> Root / "api" / "orders" / orderIdValue / "refunds" / refundIdValue / "approve" =>
      orderService
        .approveRequestedRefund(OrderId(orderIdValue), RefundId(refundIdValue), Instant.now())
        .flatMap(order => toOrderResponseDto(order).flatMap(orderResponseDto => Ok(orderResponseDto.asJson)))

    case POST -> Root / "api" / "orders" / orderIdValue / "refunds" / refundIdValue / "settle" =>
      orderService
        .settleApprovedRefund(OrderId(orderIdValue), RefundId(refundIdValue), Instant.now())
        .flatMap(order => toOrderResponseDto(order).flatMap(orderResponseDto => Ok(orderResponseDto.asJson)))
  }

  private def serveFrontendAsset(requestPath: String): F[Response[F]] =
    val normalizedRequestPath = requestPath.stripPrefix("/")
    val candidatePath =
      if normalizedRequestPath.isEmpty then frontendDistRootDirectoryPath.resolve("index.html")
      else frontendDistRootDirectoryPath.resolve(normalizedRequestPath).normalize()

    val resolvedPath =
      if candidatePath.startsWith(frontendDistRootDirectoryPath) && NioFiles.exists(candidatePath) && !NioFiles.isDirectory(candidatePath) then
        candidatePath
      else
        frontendDistRootDirectoryPath.resolve("index.html").normalize()

    Async[F].blocking(NioFiles.exists(resolvedPath)).flatMap {
      case false =>
        NotFound()
      case true =>
        Async[F].blocking(NioFiles.readAllBytes(resolvedPath)).flatMap { fileBytes =>
          val mediaType =
            resolvedPath.getFileName.toString.toLowerCase match
              case name if name.endsWith(".html") => MediaType.text.html
              case name if name.endsWith(".js")   => MediaType.text.javascript
              case name if name.endsWith(".css")  => MediaType.text.css
              case name if name.endsWith(".json") => MediaType.application.json
              case name if name.endsWith(".svg")  => MediaType.unsafeParse("image/svg+xml")
              case name if name.endsWith(".png")  => MediaType.image.png
              case name if name.endsWith(".jpg")  => MediaType.image.jpeg
              case name if name.endsWith(".jpeg") => MediaType.image.jpeg
              case name if name.endsWith(".ico")  => MediaType.unsafeParse("image/x-icon")
              case _                              => MediaType.application.`octet-stream`

          Ok(fileBytes).map(_.putHeaders(headers.`Content-Type`(mediaType)))
        }
    }

  val routes: HttpRoutes[F] =
    Kleisli { request =>
      baseRoutes.run(request).handleErrorWith(throwable => OptionT.liftF(handleDomainError(throwable)))
    }

  private object DepartureAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("departureAirport")
  private object ArrivalAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("arrivalAirport")
  private object DepartureDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("date")
  private object HotelLocationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("location")
  private object AttractionCityQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("city")
  private object FromStationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("fromStation")
  private object ToStationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("toStation")
  private object CheckInDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkInDate")
  private object CheckOutDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkOutDate")
  private object ManagerIdQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("managerId")
  private object ManagerTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("managerType")
  private object TaskStatusQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("status")

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

  private def parseRequestedSupplierReviewStatuses(taskStatusValue: Option[String]): Set[SupplierReviewStatus] =
    taskStatusValue.map(_.trim.toLowerCase).filter(_.nonEmpty) match
      case Some("confirmed") => Set(SupplierReviewStatus.SupplierConfirmed)
      case Some("rejected")  => Set(SupplierReviewStatus.SupplierRejected)
      case Some("all") =>
        Set(
          SupplierReviewStatus.PendingSupplierConfirmation,
          SupplierReviewStatus.SupplierConfirmed,
          SupplierReviewStatus.SupplierRejected
        )
      case _ => Set(SupplierReviewStatus.PendingSupplierConfirmation)

  private def fromEither[A](value: Either[? <: Throwable, A]): F[A] =
    MonadThrow[F].fromEither(value.leftMap(identity))

  private def toOrderResponseDto(order: Order): F[OrderResponseDto] =
    inventoryReservationRepository
      .findReservationsByOrderId(order.orderId)
      .map(reservations => OrderResponseDto.fromDomain(order, reservations))

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
        case InventoryReservationError.InventoryWasNotAvailable(_, _, _) =>
          Status.Conflict -> ApiErrorResponseDto("inventory_not_available", throwable.getMessage)
        case AttractionError.AttractionWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("attraction_not_found", throwable.getMessage)
        case AttractionError.TicketTypeWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("ticket_type_not_found", throwable.getMessage)
        case AttractionError.TicketTypeWasInactive(_) =>
          Status.BadRequest -> ApiErrorResponseDto("ticket_type_inactive", throwable.getMessage)
        case AttractionError.AttractionTravelerWasNotEligible(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("traveler_not_eligible", throwable.getMessage)
        case AttractionError.AttractionWasNotOwnedByManager(_, _) =>
          Status.Forbidden -> ApiErrorResponseDto("manager_scope_mismatch", throwable.getMessage)
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
        case TrainError.RailwayManagerWasNotFoundByEmail(_) | TrainError.RailwayManagerWasNotFoundById(_) =>
          Status.NotFound -> ApiErrorResponseDto("train_manager_not_found", throwable.getMessage)
        case TrainError.TrainWasNotFound(_) =>
          Status.NotFound -> ApiErrorResponseDto("train_not_found", throwable.getMessage)
        case TrainError.TrainWasNotOpenForSale(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("train_not_on_sale", throwable.getMessage)
        case TrainError.TrainStopWasNotFound(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("train_station_invalid", throwable.getMessage)
        case TrainError.TrainStationOrderWasInvalid(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("train_station_order_invalid", throwable.getMessage)
        case TrainError.TrainSegmentPriceWasMissing(_, _, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("train_price_not_defined", throwable.getMessage)
        case TrainBookingApplicationError.TravelerSelectionWasInvalid(_) =>
          Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", throwable.getMessage)
        case AttractionBookingApplicationError.OrderWasNotOwnedByUser(_, _) =>
          Status.Forbidden -> ApiErrorResponseDto("order_owner_mismatch", throwable.getMessage)
        case AvatarApplicationError.AvatarWasMissing =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_missing", throwable.getMessage)
        case AvatarApplicationError.AvatarFileTypeWasInvalid(_) | AvatarApplicationError.AvatarFileExtensionWasInvalid(_) =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_type_invalid", throwable.getMessage)
        case AvatarApplicationError.AvatarFileWasTooLarge(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_too_large", throwable.getMessage)
        case AvatarApplicationError.AvatarUploadFailed(_) =>
          Status.BadRequest -> ApiErrorResponseDto("avatar_upload_failed", throwable.getMessage)
        case ManagerError.ManagerWasNotFoundByEmail(_, _) | ManagerError.ManagerWasNotFoundById(_, _) =>
          Status.NotFound -> ApiErrorResponseDto("manager_not_found", throwable.getMessage)
        case ManagerError.ManagerEmailAlreadyExists(_) =>
          Status.Conflict -> ApiErrorResponseDto("manager_email_exists", throwable.getMessage)
        case ManagerError.ManagerWasInactive(_, _) =>
          Status.Forbidden -> ApiErrorResponseDto("manager_inactive", throwable.getMessage)
        case ManagerError.ManagerScopeDidNotMatch(_, _, _) =>
          Status.Forbidden -> ApiErrorResponseDto("manager_scope_mismatch", throwable.getMessage)
        case TravelerError.TravelerDocumentNumberAlreadyExists(_) =>
          Status.Conflict -> ApiErrorResponseDto("traveler_document_exists", throwable.getMessage)
        case OrderError.SupplierRejectReasonWasEmpty(_) =>
          Status.BadRequest -> ApiErrorResponseDto("decision_reason_required", throwable.getMessage)
        case OrderError.OrderItemWasNotFound(_, _) =>
          Status.NotFound -> ApiErrorResponseDto("order_item_not_found", throwable.getMessage)
        case OrderError.OrderItemWasNotAwaitingSupplierDecision(_, _) =>
          Status.BadRequest -> ApiErrorResponseDto("order_item_not_actionable", throwable.getMessage)
        case OrderError.OrderCurrencyDidNotMatch(_, _, _) =>
          Status.BadRequest -> ApiErrorResponseDto("currency_mismatch", throwable.getMessage)
        case OrderError.PaymentWasAlreadyCompleted(_) =>
          Status.BadRequest -> ApiErrorResponseDto("payment_already_completed", throwable.getMessage)
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
      orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
      flightBookingApplicationService: FlightBookingApplicationService[F],
      hotelBookingApplicationService: HotelBookingApplicationService[F],
      trainBookingApplicationService: TrainBookingApplicationService[F],
      trainAdminApplicationService: TrainAdminApplicationService[F],
      attractionBookingApplicationService: AttractionBookingApplicationService[F],
      attractionAdminApplicationService: AttractionAdminApplicationService[F],
      managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
      avatarApplicationService: AvatarApplicationService[F],
      userRepository: UserRepository[F],
      travelerProfileRepository: TravelerProfileRepository[F],
      orderRepository: OrderRepository[F],
      inventoryReservationRepository: InventoryReservationRepository[F],
      avatarUploadRootDirectoryPath: Path,
      frontendDistRootDirectoryPath: Path
  ): ApiRouter[F] =
    new ApiRouter[F](
      userService,
      travelerProfileService,
      orderService,
      orderLifecycleApplicationService,
      flightBookingApplicationService,
      hotelBookingApplicationService,
      trainBookingApplicationService,
      trainAdminApplicationService,
      attractionBookingApplicationService,
      attractionAdminApplicationService,
      managerWorkflowApplicationService,
      avatarApplicationService,
      userRepository,
      travelerProfileRepository,
      orderRepository,
      inventoryReservationRepository,
      avatarUploadRootDirectoryPath,
      frontendDistRootDirectoryPath
    )
