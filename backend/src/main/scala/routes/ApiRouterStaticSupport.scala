package com.typesafe.travel.api

import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.{ApiErrorResponseDto, HealthResponseDto}
import com.typesafe.travel.attraction.domain.AttractionError
import com.typesafe.travel.auth.domain.AuthError
import com.typesafe.travel.content.domain.{BlogError, ReviewError}
import com.typesafe.travel.flight.domain.FlightError
import com.typesafe.travel.hotel.domain.HotelError
import com.typesafe.travel.identity.domain.UserError
import com.typesafe.travel.inventory.domain.InventoryReservationError
import com.typesafe.travel.operations.domain.ManagerError
import com.typesafe.travel.order.domain.OrderError
import com.typesafe.travel.shared.kernel.SharedValidationError
import com.typesafe.travel.tourgroup.domain.TourGroupError
import com.typesafe.travel.train.domain.TrainError
import io.circe.syntax.*
import fs2.Stream
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.headers
import org.typelevel.ci.CIString

import java.nio.file.{Files => NioFiles, Path => FsPath}

trait ApiRouterStaticSupport[F[_]: Async]:
  self: ApiRouter[F] =>

  import JsonCodecs.given

  protected def domainRoutes: HttpRoutes[F]

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

          Ok(Stream.emits(fileBytes).covary[F]).map(_.putHeaders(headers.`Content-Type`(mediaType)))
        }
    }

  protected val healthAndStaticRoutes: HttpRoutes[F] =
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
        serveUploadFile(avatarUploadRootDirectoryPath, fileNameValue.trim)

      case GET -> Root / "uploads" / "content" / collectionValue / fileNameValue =>
        val normalizedCollection = collectionValue.trim.toLowerCase
        val normalizedFileName = fileNameValue.trim
        if normalizedCollection.isEmpty then NotFound()
        else serveUploadFile(contentUploadRootDirectoryPath.resolve(normalizedCollection).normalize(), normalizedFileName)

      case GET -> Root / "uploads" / "assets" / assetIdValue =>
        serveUploadedBinaryAsset(assetIdValue.trim)

      case GET -> Root / "uploads" / "assets" / assetIdValue / _ =>
        serveUploadedBinaryAsset(assetIdValue.trim)

      case GET -> path =>
        serveFrontendAsset(path.renderString)
    }

  val routes: HttpRoutes[F] =
    Kleisli { request =>
      (self.domainRoutes <+> healthAndStaticRoutes).run(request).handleErrorWith(throwable => OptionT.liftF(handleDomainError(throwable)))
    }

  private def serveUploadFile(rootDirectoryPath: FsPath, fileName: String): F[Response[F]] =
    if fileName.isEmpty || fileName.contains("\\") || fileName.contains("/") then
      NotFound()
    else
      val filePath = rootDirectoryPath.resolve(fileName).normalize()
      if filePath.startsWith(rootDirectoryPath) then
        Async[F].blocking(NioFiles.exists(filePath)).flatMap {
          case false =>
            NotFound()
          case true =>
            Async[F].blocking(NioFiles.readAllBytes(filePath)).flatMap { fileBytes =>
              Ok(Stream.emits(fileBytes).covary[F]).map(
                _.putHeaders(
                  headers.`Content-Type`(inferMediaType(fileName)),
                  Header.Raw(CIString("Access-Control-Allow-Origin"), "*"),
                  Header.Raw(CIString("Cross-Origin-Resource-Policy"), "cross-origin"),
                  Header.Raw(CIString("Cache-Control"), "no-store, no-cache, must-revalidate"),
                  Header.Raw(CIString("Content-Disposition"), "inline")
                )
              )
            }
        }
      else
        NotFound()

  private def serveUploadedBinaryAsset(assetId: String): F[Response[F]] =
    if assetId.isEmpty then
      NotFound()
    else
      uploadedBinaryAssetRepository match
        case None => NotFound()
        case Some(assetRepository) =>
          assetRepository.findAssetByAssetId(assetId).flatMap {
            case None => NotFound()
            case Some(asset) =>
              Ok(Stream.emits(asset.binaryContent).covary[F]).map(
                _.putHeaders(
                  headers.`Content-Type`(MediaType.unsafeParse(asset.mimeType)),
                  Header.Raw(CIString("Access-Control-Allow-Origin"), "*"),
                  Header.Raw(CIString("Cross-Origin-Resource-Policy"), "cross-origin"),
                  Header.Raw(CIString("Cache-Control"), "no-store, no-cache, must-revalidate"),
                  Header.Raw(CIString("Content-Disposition"), "inline")
                )
              )
          }

  private def inferMediaType(fileName: String): MediaType =
    fileName.toLowerCase match
      case name if name.endsWith(".png")  => MediaType.image.png
      case name if name.endsWith(".jpg")  => MediaType.image.jpeg
      case name if name.endsWith(".jpeg") => MediaType.image.jpeg
      case name if name.endsWith(".webp") => MediaType.unsafeParse("image/webp")
      case name if name.endsWith(".svg")  => MediaType.unsafeParse("image/svg+xml")
      case name if name.endsWith(".json") => MediaType.application.json
      case name if name.endsWith(".css")  => MediaType.text.css
      case name if name.endsWith(".js")   => MediaType.text.javascript
      case _                              => MediaType.application.`octet-stream`

  protected def handleDomainError(throwable: Throwable): F[Response[F]] =
    val (responseStatus, apiErrorResponseDto) =
      throwable match
        case UserError.UserEmailAddressAlreadyExists(_) => Status.Conflict -> ApiErrorResponseDto("user_email_exists", throwable.getMessage)
        case UserError.UserWasNotFoundByEmail(_) | UserError.UserWasNotFound(_) => Status.NotFound -> ApiErrorResponseDto("user_not_found", throwable.getMessage)
        case flightError: FlightError if flightError.code == "flight_not_found" => Status.NotFound -> ApiErrorResponseDto("flight_not_found", throwable.getMessage)
        case applicationError: FlightBookingApplicationError if applicationError.code == "flight_cabin_not_found" =>
          Status.BadRequest -> ApiErrorResponseDto("cabin_not_found", applicationError.message)
        case applicationError: FlightBookingApplicationError if applicationError.code == "flight_cabin_not_bookable" =>
          Status.BadRequest -> ApiErrorResponseDto("cabin_not_bookable", applicationError.message)
        case applicationError: FlightBookingApplicationError if applicationError.code == "flight_traveler_selection_invalid" =>
          Status.BadRequest -> ApiErrorResponseDto("invalid_traveler_selection", applicationError.message)
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
        case com.typesafe.travel.traveler.domain.TravelerError.TravelerDocumentNumberAlreadyExists(_) => Status.Conflict -> ApiErrorResponseDto("traveler_document_exists", throwable.getMessage)
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
