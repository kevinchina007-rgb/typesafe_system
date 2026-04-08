package com.typesafe.travel.api

import com.typesafe.travel.api.application.*
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.traveler.domain.*
import com.typesafe.travel.persistence.*
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.http4s.HttpApp

final case class ApplicationWiring[F[_]](
    userService: UserService[F],
    travelerProfileService: TravelerProfileService[F],
    flightService: FlightService[F],
    hotelService: HotelService[F],
    trainService: TrainService[F],
    ticketEligibilityService: TicketEligibilityService[F],
    flightInventoryLockingService: FlightInventoryLockingService[F],
    hotelInventoryLockingService: HotelInventoryLockingService[F],
    trainInventoryLockingService: TrainInventoryLockingService[F],
    orderService: OrderService[F],
    orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    authApplicationService: AuthApplicationService[F],
    managerService: ManagerService[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    trainBookingApplicationService: TrainBookingApplicationService[F],
    attractionBookingApplicationService: AttractionBookingApplicationService[F],
    blogApplicationService: BlogApplicationService[F],
    reviewApplicationService: ReviewApplicationService[F],
    tourGroupApplicationService: TourGroupApplicationService[F],
    trainAdminApplicationService: TrainAdminApplicationService[F],
    attractionAdminApplicationService: AttractionAdminApplicationService[F],
    managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
    avatarApplicationService: AvatarApplicationService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    flightRepository: FlightRepository[F],
    hotelRepository: HotelRepository[F],
    trainRepository: TrainRepository[F],
    attractionRepository: AttractionRepository[F],
    blogRepository: BlogRepository[F],
    reviewRepository: ReviewRepository[F],
    authRepository: AuthRepository[F],
    tourGroupRepository: TourGroupRepository[F],
    orderRepository: OrderRepository[F],
    inventoryReservationRepository: InventoryReservationRepository[F],
    managerRepository: ManagerRepository[F],
    httpApp: HttpApp[F]
)

object ApplicationWiring:
  def create[F[_]: Async]: F[ApplicationWiring[F]] =
    resource[F].use(wiring => Async[F].pure(wiring))

  def resource[F[_]: Async]: Resource[F, ApplicationWiring[F]] =
    DatabaseConfig.loadFromEnvironment.repositoryMode match
      case RepositoryMode.InMemory =>
        Resource.pure(InMemoryApplicationWiring.create[F])
      case RepositoryMode.Database =>
        PersistenceApplicationWiring.resource[F]
