package com.typesafe.travel.api

import cats.MonadThrow
import cats.effect.LiftIO
import cats.effect.IO
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
import com.typesafe.travel.persistence.UploadedBinaryAssetRepository
import org.http4s.*
import org.http4s.dsl.Http4sDsl

import java.nio.file.Path

final class ApiRouter[F[_]: Async: Clock: LiftIO](
    protected val userService: UserService[F],
    protected val travelerProfileService: TravelerProfileService[F],
    protected val orderService: OrderService[F],
    protected val orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    protected val authApplicationService: AuthApplicationService[F],
    protected val flightBookingApplicationService: FlightBookingApplicationService,
    protected val hotelBookingApplicationService: HotelBookingApplicationService[F],
    protected val trainBookingApplicationService: TrainBookingApplicationService[F],
    protected val trainAdminApplicationService: TrainAdminApplicationService[F],
    protected val attractionBookingApplicationService: AttractionBookingApplicationService[F],
    protected val blogApplicationService: BlogApplicationService[F],
    protected val reviewApplicationService: ReviewApplicationService[F],
    protected val feedbackApplicationService: FeedbackApplicationService[F],
    protected val advertisementApplicationService: AdvertisementApplicationService[F],
    protected val attractionAdminApplicationService: AttractionAdminApplicationService[F],
    protected val tourGroupApplicationService: TourGroupApplicationService[F],
    protected val managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
    protected val avatarApplicationService: AvatarApplicationService[F],
    protected val userRepository: UserRepository[F],
    protected val travelerProfileRepository: TravelerProfileRepository[F],
    protected val orderRepository: OrderRepository[F],
    protected val inventoryReservationRepository: InventoryReservationRepository[F],
    protected val uploadedBinaryAssetRepository: Option[UploadedBinaryAssetRepository[F]],
    protected val avatarUploadRootDirectoryPath: Path,
    protected val contentUploadRootDirectoryPath: Path,
    protected val frontendDistRootDirectoryPath: Path
) extends Http4sDsl[F]
    with RouteTransportSupport[F]
    with ApiRouterSessionSupport[F]
    with ApiRouterRequestSupport[F]
    with ApiRouterStaticSupport[F]
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
    with FeedbackApiRoutes[F]
    with AdvertisementApiRoutes[F]
    with ExploreApiRoutes[F]
    with ManagerApiRoutes[F]
    with OrderApiRoutes[F]
    with TourGroupApiRoutes[F]:

  override protected val domainRoutes: HttpRoutes[F] =
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
      feedbackRoutes <+>
      advertisementRoutes <+>
      exploreRoutes <+>
      managerRoutes <+>
      orderRoutes <+>
      tourGroupRoutes

object ApiRouter:
  def apply[F[_]: Async: Clock: LiftIO](
      userService: UserService[F],
      travelerProfileService: TravelerProfileService[F],
      orderService: OrderService[F],
      orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
      authApplicationService: AuthApplicationService[F],
      flightBookingApplicationService: FlightBookingApplicationService,
      hotelBookingApplicationService: HotelBookingApplicationService[F],
      trainBookingApplicationService: TrainBookingApplicationService[F],
      trainAdminApplicationService: TrainAdminApplicationService[F],
      attractionBookingApplicationService: AttractionBookingApplicationService[F],
      blogApplicationService: BlogApplicationService[F],
      reviewApplicationService: ReviewApplicationService[F],
      feedbackApplicationService: FeedbackApplicationService[F],
      advertisementApplicationService: AdvertisementApplicationService[F],
      attractionAdminApplicationService: AttractionAdminApplicationService[F],
      tourGroupApplicationService: TourGroupApplicationService[F],
      managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
      avatarApplicationService: AvatarApplicationService[F],
      userRepository: UserRepository[F],
      travelerProfileRepository: TravelerProfileRepository[F],
      orderRepository: OrderRepository[F],
      inventoryReservationRepository: InventoryReservationRepository[F],
      uploadedBinaryAssetRepository: Option[UploadedBinaryAssetRepository[F]],
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
      feedbackApplicationService,
      advertisementApplicationService,
      attractionAdminApplicationService,
      tourGroupApplicationService,
      managerWorkflowApplicationService,
      avatarApplicationService,
      userRepository,
      travelerProfileRepository,
      orderRepository,
      inventoryReservationRepository,
      uploadedBinaryAssetRepository,
      avatarUploadRootDirectoryPath,
      contentUploadRootDirectoryPath,
      frontendDistRootDirectoryPath
    )
