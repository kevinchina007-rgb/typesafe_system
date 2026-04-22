package com.typesafe.travel.api

import cats.effect.IO
import cats.effect.kernel.Resource
import com.typesafe.travel.api.application.*
import com.typesafe.travel.advertising.domain.AdvertisementRepository
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.traveler.domain.*
import org.http4s.HttpApp

final case class ApplicationWiring(
    userService: UserService[IO],
    travelerProfileService: TravelerProfileService[IO],
    flightService: FlightService[IO],
    hotelService: HotelService[IO],
    trainService: TrainService[IO],
    ticketEligibilityService: TicketEligibilityService[IO],
    flightInventoryLockingService: FlightInventoryLockingService[IO],
    hotelInventoryLockingService: HotelInventoryLockingService[IO],
    trainInventoryLockingService: TrainInventoryLockingService[IO],
    orderService: OrderService[IO],
    orderLifecycleApplicationService: OrderLifecycleApplicationService[IO],
    authApplicationService: AuthApplicationService[IO],
    managerService: ManagerService[IO],
    flightBookingApplicationService: FlightBookingApplicationService,
    hotelBookingApplicationService: HotelBookingApplicationService[IO],
    trainBookingApplicationService: TrainBookingApplicationService[IO],
    attractionBookingApplicationService: AttractionBookingApplicationService[IO],
    blogApplicationService: BlogApplicationService[IO],
    reviewApplicationService: ReviewApplicationService[IO],
    feedbackApplicationService: FeedbackApplicationService[IO],
    advertisementApplicationService: AdvertisementApplicationService[IO],
    tourGroupApplicationService: TourGroupApplicationService[IO],
    trainAdminApplicationService: TrainAdminApplicationService[IO],
    attractionAdminApplicationService: AttractionAdminApplicationService[IO],
    managerWorkflowApplicationService: ManagerWorkflowApplicationService[IO],
    avatarApplicationService: AvatarApplicationService[IO],
    userRepository: UserRepository[IO],
    travelerProfileRepository: TravelerProfileRepository[IO],
    flightRepository: FlightRepository[IO],
    hotelRepository: HotelRepository[IO],
    trainRepository: TrainRepository[IO],
    attractionRepository: AttractionRepository[IO],
    blogRepository: BlogRepository[IO],
    reviewRepository: ReviewRepository[IO],
    feedbackRepository: FeedbackRepository[IO],
    advertisementRepository: AdvertisementRepository[IO],
    authRepository: AuthRepository[IO],
    tourGroupRepository: TourGroupRepository[IO],
    orderRepository: OrderRepository[IO],
    inventoryReservationRepository: InventoryReservationRepository[IO],
    managerRepository: ManagerRepository[IO],
    httpApp: HttpApp[IO]
)

object ApplicationWiring:
  def create: IO[ApplicationWiring] =
    resource.use(wiring => IO.pure(wiring))

  def resource: Resource[IO, ApplicationWiring] =
    DatabaseConfig.loadFromEnvironment.repositoryMode match
      case RepositoryMode.InMemory =>
        Resource.pure(InMemoryApplicationWiring.create)
      case RepositoryMode.Database =>
        PersistenceApplicationWiring.resource
