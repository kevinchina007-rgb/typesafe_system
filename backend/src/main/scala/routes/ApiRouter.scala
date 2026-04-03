package com.typesafe.travel.api

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.{Async, Clock}
import cats.syntax.all.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.api.routes.*
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.traveler.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers
import org.http4s.multipart.Multipart

import java.nio.file.{Files => NioFiles, Path}
import java.time.{Instant, LocalDate}

final class ApiRouter[F[_]: Async: Clock](
    protected val userService: UserService[F],
    protected val travelerProfileService: TravelerProfileService[F],
    protected val orderService: OrderService[F],
    protected val orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    protected val authApplicationService: AuthApplicationService[F],
    protected val flightBookingApplicationService: FlightBookingApplicationService[F],
    protected val hotelBookingApplicationService: HotelBookingApplicationService[F],
    protected val trainBookingApplicationService: TrainBookingApplicationService[F],
    protected val trainAdminApplicationService: TrainAdminApplicationService[F],
    protected val attractionBookingApplicationService: AttractionBookingApplicationService[F],
    protected val blogApplicationService: BlogApplicationService[F],
    protected val reviewApplicationService: ReviewApplicationService[F],
    protected val attractionAdminApplicationService: AttractionAdminApplicationService[F],
    protected val tourGroupApplicationService: TourGroupApplicationService[F],
    protected val managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
    protected val avatarApplicationService: AvatarApplicationService[F],
    protected val userRepository: UserRepository[F],
    protected val travelerProfileRepository: TravelerProfileRepository[F],
    protected val orderRepository: OrderRepository[F],
    protected val inventoryReservationRepository: InventoryReservationRepository[F],
    protected val avatarUploadRootDirectoryPath: Path,
    protected val contentUploadRootDirectoryPath: Path,
    protected val frontendDistRootDirectoryPath: Path
) extends Http4sDsl[F]
    with AuthApiRoutes[F]
    with ManagerAuthApiRoutes[F]
    with IdentityApiRoutes[F]
    with TravelerApiRoutes[F]
    with FlightApiRoutes[F]
    with HotelApiRoutes[F]
    with TrainApiRoutes[F]
    with AttractionApiRoutes[F]
    with BlogApiRoutes[F]
    with ReviewApiRoutes[F]
    with ExploreApiRoutes[F]
    with ManagerApiRoutes[F]
    with OrderApiRoutes[F]
    with TourGroupApiRoutes[F]:

  import JsonCodecs.given

  protected val userSessionCookieName = "travel_user_session"
  protected val managerSessionCookieName = "travel_manager_session"

  protected def currentInstantF: F[Instant] =
    Clock[F].realTimeInstant

  protected def currentLocalDateF: F[LocalDate] =
    currentInstantF.map(_.atZone(java.time.ZoneId.systemDefault()).toLocalDate)

  protected def currentUserSessionId(request: Request[F]): Option[SessionId] =
    request.cookies.find(_.name == userSessionCookieName).map(cookie => SessionId(cookie.content))

  protected def currentManagerSessionId(request: Request[F]): Option[SessionId] =
    request.cookies.find(_.name == managerSessionCookieName).map(cookie => SessionId(cookie.content))

  protected def requireCurrentUserId(request: Request[F]): F[UserId] =
    for
      sessionId <- currentUserSessionId(request).liftTo[F](AuthError.UserSessionWasRequired)
      now <- currentInstantF
      userSession <- authApplicationService.restoreCurrentUser(sessionId, now)
    yield userSession.user.userId

  protected def requireCurrentManager(request: Request[F]): F[CurrentManagerSessionView] =
    for
      sessionId <- currentManagerSessionId(request).liftTo[F](AuthError.ManagerSessionWasRequired)
      now <- currentInstantF
      managerSession <- authApplicationService.restoreCurrentManager(sessionId, now)
    yield managerSession

  protected def withUserSessionCookie(response: Response[F], sessionId: SessionId, expiresAt: Instant): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = userSessionCookieName,
        content = sessionId.value,
        httpOnly = true,
        secure = false,
        path = Some("/"),
        sameSite = Some(SameSite.Lax),
        expires = Some(HttpDate.unsafeFromInstant(expiresAt))
      )
    )

  protected def clearUserSessionCookie(response: Response[F]): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = userSessionCookieName,
        content = "",
        httpOnly = true,
        secure = false,
        path = Some("/"),
        sameSite = Some(SameSite.Lax),
        expires = Some(HttpDate.Epoch)
      )
    )

  protected def withManagerSessionCookie(response: Response[F], sessionId: SessionId, expiresAt: Instant): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = managerSessionCookieName,
        content = sessionId.value,
        httpOnly = true,
        secure = false,
        path = Some("/"),
        sameSite = Some(SameSite.Lax),
        expires = Some(HttpDate.unsafeFromInstant(expiresAt))
      )
    )

  protected def clearManagerSessionCookie(response: Response[F]): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = managerSessionCookieName,
        content = "",
        httpOnly = true,
        secure = false,
        path = Some("/"),
        sameSite = Some(SameSite.Lax),
        expires = Some(HttpDate.Epoch)
      )
    )

  protected given createUserDecoder: EntityDecoder[F, CreateUserRequestDto] = jsonOf[F, CreateUserRequestDto]
  protected given loginUserDecoder: EntityDecoder[F, LoginUserRequestDto] = jsonOf[F, LoginUserRequestDto]
  protected given signupRequestDecoder: EntityDecoder[F, SignupRequestDto] = jsonOf[F, SignupRequestDto]
  protected given passwordLoginDecoder: EntityDecoder[F, PasswordLoginRequestDto] = jsonOf[F, PasswordLoginRequestDto]
  protected given managerPasswordLoginDecoder: EntityDecoder[F, ManagerPasswordLoginRequestDto] = jsonOf[F, ManagerPasswordLoginRequestDto]
  protected given createTravelerDecoder: EntityDecoder[F, CreateTravelerRequestDto] = jsonOf[F, CreateTravelerRequestDto]
  protected given createOrderDecoder: EntityDecoder[F, CreateOrderRequestDto] = jsonOf[F, CreateOrderRequestDto]
  protected given managerLoginDecoder: EntityDecoder[F, ManagerLoginRequestDto] = jsonOf[F, ManagerLoginRequestDto]
  protected given managerDecisionDecoder: EntityDecoder[F, ManagerDecisionRequestDto] = jsonOf[F, ManagerDecisionRequestDto]
  protected given managerBatchDecisionDecoder: EntityDecoder[F, ManagerBatchDecisionRequestDto] = jsonOf[F, ManagerBatchDecisionRequestDto]
  protected given registerAirlineManagerDecoder: EntityDecoder[F, RegisterAirlineManagerRequestDto] = jsonOf[F, RegisterAirlineManagerRequestDto]
  protected given registerHotelManagerDecoder: EntityDecoder[F, RegisterHotelManagerRequestDto] = jsonOf[F, RegisterHotelManagerRequestDto]
  protected given createManagerFlightDecoder: EntityDecoder[F, CreateManagerFlightRequestDto] = jsonOf[F, CreateManagerFlightRequestDto]
  protected given createManagerRoomTypeDecoder: EntityDecoder[F, CreateManagerRoomTypeRequestDto] = jsonOf[F, CreateManagerRoomTypeRequestDto]
  protected given registerRailwayManagerDecoder: EntityDecoder[F, RegisterRailwayManagerRequestDto] = jsonOf[F, RegisterRailwayManagerRequestDto]
  protected given trainAdminLoginDecoder: EntityDecoder[F, TrainAdminLoginRequestDto] = jsonOf[F, TrainAdminLoginRequestDto]
  protected given createTrainJourneyDecoder: EntityDecoder[F, CreateTrainJourneyRequestDto] = jsonOf[F, CreateTrainJourneyRequestDto]
  protected given registerAttractionManagerDecoder: EntityDecoder[F, RegisterAttractionManagerRequestDto] = jsonOf[F, RegisterAttractionManagerRequestDto]
  protected given attractionAdminLoginDecoder: EntityDecoder[F, AttractionAdminLoginRequestDto] = jsonOf[F, AttractionAdminLoginRequestDto]
  protected given createAttractionDecoder: EntityDecoder[F, CreateAttractionRequestDto] = jsonOf[F, CreateAttractionRequestDto]
  protected given createTicketTypeDecoder: EntityDecoder[F, CreateTicketTypeRequestDto] = jsonOf[F, CreateTicketTypeRequestDto]
  protected given createTicketEligibilityRuleDecoder: EntityDecoder[F, CreateTicketEligibilityRuleRequestDto] = jsonOf[F, CreateTicketEligibilityRuleRequestDto]
  protected given addAttractionItemDecoder: EntityDecoder[F, BookAttractionItemRequestDto] = jsonOf[F, BookAttractionItemRequestDto]
  protected given addFlightItemDecoder: EntityDecoder[F, BookFlightRequestDto] = jsonOf[F, BookFlightRequestDto]
  protected given addHotelItemDecoder: EntityDecoder[F, BookHotelRequestDto] = jsonOf[F, BookHotelRequestDto]
  protected given addTrainItemDecoder: EntityDecoder[F, BookTrainItemRequestDto] = jsonOf[F, BookTrainItemRequestDto]
  protected given authorizePaymentDecoder: EntityDecoder[F, PayOrderRequestDto] = jsonOf[F, PayOrderRequestDto]
  protected given requestRefundDecoder: EntityDecoder[F, RequestRefundRequestDto] = jsonOf[F, RequestRefundRequestDto]
  protected given createTourGroupDecoder: EntityDecoder[F, CreateTourGroupRequestDto] = jsonOf[F, CreateTourGroupRequestDto]
  protected given joinTourGroupDecoder: EntityDecoder[F, JoinTourGroupRequestDto] = jsonOf[F, JoinTourGroupRequestDto]
  protected given addMembershipTravelerDecoder: EntityDecoder[F, AddMembershipTravelerRequestDto] = jsonOf[F, AddMembershipTravelerRequestDto]
  protected given createGroupPlanItemDecoder: EntityDecoder[F, CreateGroupPlanItemRequestDto] = jsonOf[F, CreateGroupPlanItemRequestDto]
  protected given createGroupPlanOptionDecoder: EntityDecoder[F, CreateGroupPlanOptionRequestDto] = jsonOf[F, CreateGroupPlanOptionRequestDto]
  protected given createGroupPlanSelectionDecoder: EntityDecoder[F, CreateGroupPlanSelectionRequestDto] = jsonOf[F, CreateGroupPlanSelectionRequestDto]
  protected given submitGroupPlanSelectionDecoder: EntityDecoder[F, SubmitGroupPlanSelectionRequestDto] = jsonOf[F, SubmitGroupPlanSelectionRequestDto]
  protected given reviewGroupPlanSelectionDecoder: EntityDecoder[F, ReviewGroupPlanSelectionRequestDto] = jsonOf[F, ReviewGroupPlanSelectionRequestDto]
  protected given rejectGroupPlanSelectionDecoder: EntityDecoder[F, RejectGroupPlanSelectionRequestDto] = jsonOf[F, RejectGroupPlanSelectionRequestDto]
  protected given payGroupPlanSelectionDecoder: EntityDecoder[F, PayGroupPlanSelectionRequestDto] = jsonOf[F, PayGroupPlanSelectionRequestDto]
  protected given batchReviewGroupPlanSelectionsDecoder: EntityDecoder[F, BatchReviewGroupPlanSelectionsRequestDto] = jsonOf[F, BatchReviewGroupPlanSelectionsRequestDto]
  protected given batchRejectGroupPlanSelectionsDecoder: EntityDecoder[F, BatchRejectGroupPlanSelectionsRequestDto] = jsonOf[F, BatchRejectGroupPlanSelectionsRequestDto]
  protected given batchPayGroupPlanSelectionsDecoder: EntityDecoder[F, BatchPayGroupPlanSelectionsRequestDto] = jsonOf[F, BatchPayGroupPlanSelectionsRequestDto]
  protected given multipartDecoder: EntityDecoder[F, Multipart[F]] = EntityDecoder.multipart[F]

  private val baseRoutes: HttpRoutes[F] =
    authRoutes <+>
      managerAuthRoutes <+>
      identityRoutes <+>
      travelerRoutes <+>
      flightRoutes <+>
      hotelRoutes <+>
      trainRoutes <+>
      attractionRoutes <+>
      blogRoutes <+>
      reviewRoutes <+>
      exploreRoutes <+>
      managerRoutes <+>
      orderRoutes <+>
      tourGroupRoutes <+>
      HttpRoutes.of[F] {
        case GET -> Root / "api" / "health" =>
          Ok(
            HealthResponseDto(
              status = "ok",
              service = "travel-platform-backend",
              backendPort = sys.env.get("TRAVEL_BACKEND_PORT").flatMap(_.trim.toIntOption).getOrElse(19095)
            ).asJson
          )

        case GET -> Root / "uploads" / "avatars" / fileNameValue =>
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

        case GET -> Root / "uploads" / "content" / collectionValue / fileNameValue =>
          val normalizedCollection = collectionValue.trim.toLowerCase
          val normalizedFileName = fileNameValue.trim
          if normalizedCollection.isEmpty || normalizedFileName.isEmpty || normalizedFileName.contains("\\") || normalizedFileName.contains("/") then
            NotFound()
          else
            val collectionDirectoryPath = contentUploadRootDirectoryPath.resolve(normalizedCollection).normalize()
            val contentFilePath = collectionDirectoryPath.resolve(normalizedFileName).normalize()
            if contentFilePath.startsWith(collectionDirectoryPath) then
              Async[F].blocking(NioFiles.exists(contentFilePath)).flatMap {
                case false =>
                  NotFound()
                case true =>
                  Async[F].blocking(NioFiles.readAllBytes(contentFilePath)).flatMap { fileBytes =>
                    Ok(fileBytes).map(
                      _.putHeaders(
                        headers.`Content-Type`(
                          normalizedFileName.toLowerCase match
                            case name if name.endsWith(".png")  => MediaType.image.png
                            case name if name.endsWith(".jpg")  => MediaType.image.jpeg
                            case name if name.endsWith(".jpeg") => MediaType.image.jpeg
                            case name if name.endsWith(".webp") => MediaType.unsafeParse("image/webp")
                            case _                              => MediaType.application.`octet-stream`
                        )
                      )
                    )
                  }
              }
            else
              NotFound()

        case GET -> path =>
          serveFrontendAsset(path.renderString)
      }

  protected def serveFrontendAsset(requestPath: String): F[Response[F]] =
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

  protected object DepartureAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("departureAirport")
  protected object ArrivalAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("arrivalAirport")
  protected object DepartureDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("date")
  protected object HotelLocationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("location")
  protected object AttractionCityQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("city")
  protected object FromStationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("fromStation")
  protected object ToStationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("toStation")
  protected object CheckInDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkInDate")
  protected object CheckOutDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkOutDate")
  protected object ManagerIdQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("managerId")
  protected object ManagerTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("managerType")
  protected object TaskStatusQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("status")
  protected object TaskResourceTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("resourceType")
  protected object SearchQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("q")
  protected object ExploreTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("type")

  protected def parseOptionalSearchText(searchTextValue: Option[String]): F[Option[String]] =
    searchTextValue match
      case Some(value) if value.trim.nonEmpty => MonadThrow[F].pure(Some(value.trim))
      case _                                  => MonadThrow[F].pure(None)

  protected def parseOptionalDate(dateValue: Option[String]): F[Option[LocalDate]] =
    dateValue match
      case Some(value) if value.trim.nonEmpty => MonadThrow[F].catchNonFatal(LocalDate.parse(value)).map(Some.apply)
      case _                                  => MonadThrow[F].pure(None)

  protected def parseOptionalStayPeriod(checkInDateValue: Option[String], checkOutDateValue: Option[String]): F[Option[StayPeriod]] =
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

  protected def createTravelerEmergencyContact(createTravelerRequestDto: CreateTravelerRequestDto): F[Option[TravelerEmergencyContact]] =
    (createTravelerRequestDto.emergencyContactName, createTravelerRequestDto.emergencyContactPhoneNumber) match
      case (Some(contactName), Some(contactPhoneNumber)) =>
        for
          parsedContactName <- fromEither(PersonName.create(contactName))
          parsedContactNumber <- fromEither(ContactNumber.create(contactPhoneNumber))
        yield Some(TravelerEmergencyContact.create(parsedContactName, parsedContactNumber))
      case _ =>
        MonadThrow[F].pure(None)

  protected def parseRequestedSupplierReviewStatuses(taskStatusValue: Option[String]): Set[SupplierReviewStatus] =
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

  protected def fromEither[A](value: Either[? <: Throwable, A]): F[A] =
    MonadThrow[F].fromEither(value.leftMap(identity))

  protected def toOrderResponseDto(order: Order): F[OrderResponseDto] =
    inventoryReservationRepository.findReservationsByOrderId(order.orderId).map(reservations => OrderResponseDto.fromDomain(order, reservations))

  protected def handleDomainError(throwable: Throwable): F[Response[F]] =
    val (responseStatus, apiErrorResponseDto) =
      throwable match
        case UserError.UserEmailAddressAlreadyExists(_) => Status.Conflict -> ApiErrorResponseDto("user_email_exists", throwable.getMessage)
        case UserError.UserWasNotFoundByEmail(_) | UserError.UserWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("user_not_found", throwable.getMessage)
        case FlightError.FlightWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("flight_not_found", throwable.getMessage)
        case FlightBookingApplicationError.CabinWasNotFound(_, _) => Status.BadRequest -> ApiErrorResponseDto("cabin_not_found", throwable.getMessage)
        case FlightBookingApplicationError.CabinWasNotBookable(_, _) => Status.BadRequest -> ApiErrorResponseDto("cabin_not_bookable", throwable.getMessage)
        case FlightBookingApplicationError.TravelerSelectionWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", throwable.getMessage)
        case InventoryReservationError.InventoryWasNotAvailable(_, _, _) => Status.Conflict -> ApiErrorResponseDto("inventory_not_available", throwable.getMessage)
        case AttractionError.AttractionWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("attraction_not_found", throwable.getMessage)
        case AttractionError.TicketTypeWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("ticket_type_not_found", throwable.getMessage)
        case AttractionError.TicketTypeWasInactive(_) => Status.BadRequest -> ApiErrorResponseDto("ticket_type_inactive", throwable.getMessage)
        case AttractionError.AttractionTravelerWasNotEligible(_, _, _) => Status.BadRequest -> ApiErrorResponseDto("traveler_not_eligible", throwable.getMessage)
        case AttractionError.AttractionWasNotOwnedByManager(_, _) => Status.Forbidden -> ApiErrorResponseDto("manager_scope_mismatch", throwable.getMessage)
        case blogError: BlogError => Status.BadRequest -> ApiErrorResponseDto("blog_error", blogError.message)
        case reviewError: ReviewError => Status.BadRequest -> ApiErrorResponseDto("review_error", reviewError.message)
        case HotelError.HotelWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("hotel_not_found", throwable.getMessage)
        case HotelBookingApplicationError.RoomTypeWasNotFound(_) => Status.BadRequest -> ApiErrorResponseDto("room_type_not_found", throwable.getMessage)
        case HotelBookingApplicationError.StayPeriodWasInvalid(_, _) => Status.BadRequest -> ApiErrorResponseDto("stay_period_invalid", throwable.getMessage)
        case HotelBookingApplicationError.RoomCountWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("validation_error", throwable.getMessage)
        case HotelBookingApplicationError.RoomInventoryWasNotBookable(_, _, _) => Status.BadRequest -> ApiErrorResponseDto("room_inventory_not_bookable", throwable.getMessage)
        case HotelBookingApplicationError.RoomCapacityWasExceeded(_, _, _) => Status.BadRequest -> ApiErrorResponseDto("room_capacity_exceeded", throwable.getMessage)
        case HotelBookingApplicationError.TravelerSelectionWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", throwable.getMessage)
        case TrainError.RailwayManagerWasNotFoundByEmail(_) | TrainError.RailwayManagerWasNotFoundById(_) => Status.NotFound -> ApiErrorResponseDto("train_manager_not_found", throwable.getMessage)
        case TrainError.TrainWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("train_not_found", throwable.getMessage)
        case TrainError.TrainWasNotOpenForSale(_, _, _) => Status.BadRequest -> ApiErrorResponseDto("train_not_on_sale", throwable.getMessage)
        case TrainError.TrainStopWasNotFound(_, _) => Status.BadRequest -> ApiErrorResponseDto("train_station_invalid", throwable.getMessage)
        case TrainError.TrainStationOrderWasInvalid(_, _, _) => Status.BadRequest -> ApiErrorResponseDto("train_station_order_invalid", throwable.getMessage)
        case TrainError.TrainSegmentPriceWasMissing(_, _, _, _) => Status.BadRequest -> ApiErrorResponseDto("train_price_not_defined", throwable.getMessage)
        case TrainBookingApplicationError.TravelerSelectionWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", throwable.getMessage)
        case AttractionBookingApplicationError.OrderWasNotOwnedByUser(_, _) => Status.Forbidden -> ApiErrorResponseDto("order_owner_mismatch", throwable.getMessage)
        case AvatarApplicationError.AvatarWasMissing => Status.BadRequest -> ApiErrorResponseDto("avatar_missing", throwable.getMessage)
        case AvatarApplicationError.AvatarFileTypeWasInvalid(_) | AvatarApplicationError.AvatarFileExtensionWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("avatar_type_invalid", throwable.getMessage)
        case AvatarApplicationError.AvatarFileWasTooLarge(_, _) => Status.BadRequest -> ApiErrorResponseDto("avatar_too_large", throwable.getMessage)
        case AvatarApplicationError.AvatarUploadFailed(_) => Status.BadRequest -> ApiErrorResponseDto("avatar_upload_failed", throwable.getMessage)
        case BlogImageUploadError.ImageWasMissing => Status.BadRequest -> ApiErrorResponseDto("blog_image_missing", throwable.getMessage)
        case BlogImageUploadError.ImageFileTypeWasInvalid(_) | BlogImageUploadError.ImageFileExtensionWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("blog_image_type_invalid", throwable.getMessage)
        case BlogImageUploadError.ImageFileWasTooLarge(_, _) => Status.BadRequest -> ApiErrorResponseDto("blog_image_too_large", throwable.getMessage)
        case ReviewImageUploadError.ImageWasMissing => Status.BadRequest -> ApiErrorResponseDto("review_image_missing", throwable.getMessage)
        case ReviewImageUploadError.ImageFileTypeWasInvalid(_) | ReviewImageUploadError.ImageFileExtensionWasInvalid(_) => Status.BadRequest -> ApiErrorResponseDto("review_image_type_invalid", throwable.getMessage)
        case ReviewImageUploadError.ImageFileWasTooLarge(_, _) => Status.BadRequest -> ApiErrorResponseDto("review_image_too_large", throwable.getMessage)
        case ManagerError.ManagerWasNotFoundByEmail(_, _) | ManagerError.ManagerWasNotFoundById(_, _) => Status.NotFound -> ApiErrorResponseDto("manager_not_found", throwable.getMessage)
        case ManagerError.ManagerEmailAlreadyExists(_) => Status.Conflict -> ApiErrorResponseDto("manager_email_exists", throwable.getMessage)
        case ManagerError.ManagerWasInactive(_, _) => Status.Forbidden -> ApiErrorResponseDto("manager_inactive", throwable.getMessage)
        case ManagerError.ManagerScopeDidNotMatch(_, _, _) => Status.Forbidden -> ApiErrorResponseDto("manager_scope_mismatch", throwable.getMessage)
        case AuthError.PasswordWasEmpty | AuthError.PasswordWasTooShort | AuthError.PasswordWasTooWeak | AuthError.CurrentPasswordDidNotMatch =>
          Status.BadRequest -> ApiErrorResponseDto("auth_error", throwable.getMessage)
        case authError: AuthError => Status.Unauthorized -> ApiErrorResponseDto("auth_error", authError.message)
        case TravelerError.TravelerDocumentNumberAlreadyExists(_) => Status.Conflict -> ApiErrorResponseDto("traveler_document_exists", throwable.getMessage)
        case OrderError.SupplierRejectReasonWasEmpty(_) => Status.BadRequest -> ApiErrorResponseDto("decision_reason_required", throwable.getMessage)
        case OrderError.OrderItemWasNotFound(_, _) => Status.NotFound -> ApiErrorResponseDto("order_item_not_found", throwable.getMessage)
        case OrderError.OrderItemWasNotAwaitingSupplierDecision(_, _) => Status.BadRequest -> ApiErrorResponseDto("order_item_not_actionable", throwable.getMessage)
        case OrderError.OrderCurrencyDidNotMatch(_, _, _) => Status.BadRequest -> ApiErrorResponseDto("currency_mismatch", throwable.getMessage)
        case OrderError.PaymentWasAlreadyCompleted(_) => Status.BadRequest -> ApiErrorResponseDto("payment_already_completed", throwable.getMessage)
        case OrderError.OrderWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("order_not_found", throwable.getMessage)
        case tourGroupError: TourGroupError => Status.BadRequest -> ApiErrorResponseDto("tour_group_error", tourGroupError.message)
        case tourGroupAppError: TourGroupApplicationError => Status.BadRequest -> ApiErrorResponseDto("tour_group_error", tourGroupAppError.message)
        case sharedValidationError: SharedValidationError => Status.BadRequest -> ApiErrorResponseDto("validation_error", sharedValidationError.message)
        case _ => Status.BadRequest -> ApiErrorResponseDto("bad_request", throwable.getMessage)

    Response[F](status = responseStatus).withEntity(apiErrorResponseDto.asJson).pure[F]

object ApiRouter:
  def apply[F[_]: Async: Clock](
      userService: UserService[F],
      travelerProfileService: TravelerProfileService[F],
      orderService: OrderService[F],
      orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
      authApplicationService: AuthApplicationService[F],
      flightBookingApplicationService: FlightBookingApplicationService[F],
      hotelBookingApplicationService: HotelBookingApplicationService[F],
      trainBookingApplicationService: TrainBookingApplicationService[F],
      trainAdminApplicationService: TrainAdminApplicationService[F],
      attractionBookingApplicationService: AttractionBookingApplicationService[F],
      blogApplicationService: BlogApplicationService[F],
      reviewApplicationService: ReviewApplicationService[F],
      attractionAdminApplicationService: AttractionAdminApplicationService[F],
      tourGroupApplicationService: TourGroupApplicationService[F],
      managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
      avatarApplicationService: AvatarApplicationService[F],
      userRepository: UserRepository[F],
      travelerProfileRepository: TravelerProfileRepository[F],
      orderRepository: OrderRepository[F],
      inventoryReservationRepository: InventoryReservationRepository[F],
      avatarUploadRootDirectoryPath: Path,
      contentUploadRootDirectoryPath: Path,
      frontendDistRootDirectoryPath: Path
  ): ApiRouter[F] =
    new ApiRouter[F](
      userService,
      travelerProfileService,
      orderService,
      orderLifecycleApplicationService,
      authApplicationService,
      flightBookingApplicationService,
      hotelBookingApplicationService,
      trainBookingApplicationService,
      trainAdminApplicationService,
      attractionBookingApplicationService,
      blogApplicationService,
      reviewApplicationService,
      attractionAdminApplicationService,
      tourGroupApplicationService,
      managerWorkflowApplicationService,
      avatarApplicationService,
      userRepository,
      travelerProfileRepository,
      orderRepository,
      inventoryReservationRepository,
      avatarUploadRootDirectoryPath,
      contentUploadRootDirectoryPath,
      frontendDistRootDirectoryPath
    )
