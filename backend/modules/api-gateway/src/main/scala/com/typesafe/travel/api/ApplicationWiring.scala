package com.typesafe.travel.api

import com.typesafe.travel.api.application.*
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.memory.*
import com.typesafe.travel.api.storage.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.attraction.*
import com.typesafe.travel.persistence.flight.*
import com.typesafe.travel.persistence.hotel.*
import com.typesafe.travel.persistence.identity.*
import com.typesafe.travel.persistence.inventory.*
import com.typesafe.travel.persistence.operations.*
import com.typesafe.travel.persistence.order.*
import com.typesafe.travel.persistence.tourgroup.*
import com.typesafe.travel.persistence.train.*
import com.typesafe.travel.persistence.traveler.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.traveler.domain.*
import org.http4s.HttpApp
import org.http4s.implicits.*
import java.nio.file.Paths

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
    managerService: ManagerService[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    trainBookingApplicationService: TrainBookingApplicationService[F],
    attractionBookingApplicationService: AttractionBookingApplicationService[F],
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
    tourGroupRepository: TourGroupRepository[F],
    orderRepository: OrderRepository[F],
    inventoryReservationRepository: InventoryReservationRepository[F],
    managerRepository: ManagerRepository[F],
    httpApp: HttpApp[F]
)

object ApplicationWiring:
  def create[F[_]: Async]: F[ApplicationWiring[F]] =
    DatabaseConfig.loadFromEnvironment.repositoryMode match
      case RepositoryMode.InMemory =>
        Async[F].pure(createInMemory[F])
      case RepositoryMode.Database =>
        createPersistence[F]

  private def createInMemory[F[_]: Async]: ApplicationWiring[F] =
    val inMemoryUserRepository = InMemoryUserRepository.create[F]
    val inMemoryTravelerProfileRepository = InMemoryTravelerProfileRepository.create[F]
    val inMemoryFlightRepository = InMemoryFlightRepository.create[F]
    val inMemoryHotelRepository = InMemoryHotelRepository.create[F]
    val inMemoryTrainRepository = InMemoryTrainRepository.create[F]
    val inMemoryAttractionRepository = InMemoryAttractionRepository.create[F]
    val inMemoryTourGroupRepository = InMemoryTourGroupRepository.create[F]
    val inMemoryOrderRepository = InMemoryOrderRepository.create[F]
    val inMemoryInventoryReservationRepository = InMemoryInventoryReservationRepository.create[F]
    val inMemoryManagerRepository = InMemoryManagerRepository.create[F]
    val avatarUploadRootDirectoryPath = Paths.get("uploads", "avatars").toAbsolutePath.normalize()
    val frontendDistRootDirectoryPath = Paths.get("..", "frontend", "dist").toAbsolutePath.normalize()
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)

    val liveUserService = LiveUserService[F](inMemoryUserRepository)
    val liveTravelerProfileService =
      LiveTravelerProfileService[F](inMemoryTravelerProfileRepository, inMemoryUserRepository)
    val liveFlightService = LiveFlightService[F](inMemoryFlightRepository)
    val liveHotelService = LiveHotelService[F](inMemoryHotelRepository)
    val liveTrainService = TrainService[F](inMemoryTrainRepository)
    val liveTicketEligibilityService = TicketEligibilityService[F]()
    val reservationLifecycle = LiveReservationLifecycle[F](inMemoryInventoryReservationRepository)
    val liveFlightInventoryLockingService =
      LiveFlightInventoryLockingService[F](inMemoryInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
    val liveHotelInventoryLockingService =
      LiveHotelInventoryLockingService[F](inMemoryInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
    val liveTrainInventoryLockingService =
      LiveTrainInventoryLockingService[F](inMemoryInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
    val liveOrderService = LiveOrderService[F](inMemoryOrderRepository)
    val liveOrderLifecycleApplicationService =
        LiveOrderLifecycleApplicationService[F](
          orderService = liveOrderService,
          orderRepository = inMemoryOrderRepository,
          reservationLifecycle = reservationLifecycle
        )
    val liveManagerService = LiveManagerService[F](inMemoryManagerRepository)
    val liveFlightBookingApplicationService =
      LiveFlightBookingApplicationService[F](
        flightService = liveFlightService,
        flightRepository = inMemoryFlightRepository,
        flightInventoryLockingService = liveFlightInventoryLockingService,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveHotelBookingApplicationService =
      LiveHotelBookingApplicationService[F](
        hotelService = liveHotelService,
        hotelRepository = inMemoryHotelRepository,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        hotelInventoryLockingService = liveHotelInventoryLockingService
      )
    val liveTrainBookingApplicationService =
      LiveTrainBookingApplicationService[F](
        trainService = liveTrainService,
        trainRepository = inMemoryTrainRepository,
        trainInventoryLockingService = liveTrainInventoryLockingService,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveAttractionBookingApplicationService =
      LiveAttractionBookingApplicationService[F](
        attractionRepository = inMemoryAttractionRepository,
        ticketEligibilityService = liveTicketEligibilityService,
        orderRepository = inMemoryOrderRepository,
        orderService = liveOrderService,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveTourGroupApplicationService =
      LiveTourGroupApplicationService[F](
        tourGroupRepository = inMemoryTourGroupRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        orderService = liveOrderService,
        orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
        orderRepository = inMemoryOrderRepository,
        flightBookingApplicationService = liveFlightBookingApplicationService,
        hotelBookingApplicationService = liveHotelBookingApplicationService,
        trainBookingApplicationService = liveTrainBookingApplicationService,
        attractionBookingApplicationService = liveAttractionBookingApplicationService
      )
    val liveTrainAdminApplicationService =
      LiveTrainAdminApplicationService[F](
        trainService = liveTrainService,
        trainRepository = inMemoryTrainRepository
      )
    val liveAttractionAdminApplicationService =
      LiveAttractionAdminApplicationService[F](
        managerService = liveManagerService,
        attractionRepository = inMemoryAttractionRepository
      )
    val liveManagerWorkflowApplicationService =
        LiveManagerWorkflowApplicationService[F](
          managerService = liveManagerService,
          managerRepository = inMemoryManagerRepository,
          orderRepository = inMemoryOrderRepository,
          orderService = liveOrderService,
          reservationLifecycle = reservationLifecycle,
          flightRepository = inMemoryFlightRepository,
          hotelRepository = inMemoryHotelRepository,
          attractionRepository = inMemoryAttractionRepository
        )
    val liveAvatarApplicationService =
      LiveAvatarApplicationService[F](
        userService = liveUserService,
        avatarStorage = localAvatarStorage
      )

    val apiRouter =
      ApiRouter[F](
        userService = liveUserService,
        travelerProfileService = liveTravelerProfileService,
        orderService = liveOrderService,
        orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
        flightBookingApplicationService = liveFlightBookingApplicationService,
        hotelBookingApplicationService = liveHotelBookingApplicationService,
        trainBookingApplicationService = liveTrainBookingApplicationService,
        attractionBookingApplicationService = liveAttractionBookingApplicationService,
        tourGroupApplicationService = liveTourGroupApplicationService,
        trainAdminApplicationService = liveTrainAdminApplicationService,
        attractionAdminApplicationService = liveAttractionAdminApplicationService,
        managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
        avatarApplicationService = liveAvatarApplicationService,
        userRepository = inMemoryUserRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        orderRepository = inMemoryOrderRepository,
        inventoryReservationRepository = inMemoryInventoryReservationRepository,
        avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
        frontendDistRootDirectoryPath = frontendDistRootDirectoryPath
      )

    ApplicationWiring(
      userService = liveUserService,
      travelerProfileService = liveTravelerProfileService,
      flightService = liveFlightService,
      hotelService = liveHotelService,
      trainService = liveTrainService,
      ticketEligibilityService = liveTicketEligibilityService,
      flightInventoryLockingService = liveFlightInventoryLockingService,
      hotelInventoryLockingService = liveHotelInventoryLockingService,
      trainInventoryLockingService = liveTrainInventoryLockingService,
      orderService = liveOrderService,
      orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
      managerService = liveManagerService,
      flightBookingApplicationService = liveFlightBookingApplicationService,
      hotelBookingApplicationService = liveHotelBookingApplicationService,
      trainBookingApplicationService = liveTrainBookingApplicationService,
      attractionBookingApplicationService = liveAttractionBookingApplicationService,
      tourGroupApplicationService = liveTourGroupApplicationService,
      trainAdminApplicationService = liveTrainAdminApplicationService,
      attractionAdminApplicationService = liveAttractionAdminApplicationService,
      managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
      avatarApplicationService = liveAvatarApplicationService,
      userRepository = inMemoryUserRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository,
      flightRepository = inMemoryFlightRepository,
      hotelRepository = inMemoryHotelRepository,
      trainRepository = inMemoryTrainRepository,
      attractionRepository = inMemoryAttractionRepository,
      tourGroupRepository = inMemoryTourGroupRepository,
      orderRepository = inMemoryOrderRepository,
      inventoryReservationRepository = inMemoryInventoryReservationRepository,
      managerRepository = inMemoryManagerRepository,
      httpApp = apiRouter.routes.orNotFound
    )

  private def createPersistence[F[_]: Async]: F[ApplicationWiring[F]] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val databaseTransactor = DatabaseTransactor.create[F](databaseConfig)
    val avatarUploadRootDirectoryPath = Paths.get("uploads", "avatars").toAbsolutePath.normalize()
    val frontendDistRootDirectoryPath = Paths.get("..", "frontend", "dist").toAbsolutePath.normalize()
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)

    for
      _ <- SchemaInitializer.initialize(databaseTransactor)
      doobieUserRepository = DoobieUserRepository[F](databaseTransactor)
      doobieTravelerProfileRepository = DoobieTravelerProfileRepository[F](databaseTransactor)
      doobieFlightRepository = DoobieFlightRepository[F](databaseTransactor)
      doobieHotelRepository = DoobieHotelRepository[F](databaseTransactor)
      doobieTrainRepository = DoobieTrainRepository[F](databaseTransactor)
      doobieAttractionRepository = DoobieAttractionRepository[F](databaseTransactor)
      doobieTourGroupRepository = DoobieTourGroupRepository[F](databaseTransactor)
      doobieOrderRepository = DoobieOrderRepository[F](databaseTransactor)
      doobieInventoryReservationRepository = DoobieInventoryReservationRepository[F](databaseTransactor)
      doobieManagerRepository = DoobieManagerRepository[F](databaseTransactor)
      liveUserService = LiveUserService[F](doobieUserRepository)
      liveTravelerProfileService =
        LiveTravelerProfileService[F](doobieTravelerProfileRepository, doobieUserRepository)
      liveFlightService = LiveFlightService[F](doobieFlightRepository)
      liveHotelService = LiveHotelService[F](doobieHotelRepository)
      liveTrainService = TrainService[F](doobieTrainRepository)
      liveTicketEligibilityService = TicketEligibilityService[F]()
      reservationLifecycle = LiveReservationLifecycle[F](doobieInventoryReservationRepository)
      liveFlightInventoryLockingService =
        LiveFlightInventoryLockingService[F](doobieInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
      liveHotelInventoryLockingService =
        LiveHotelInventoryLockingService[F](doobieInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
      liveTrainInventoryLockingService =
        LiveTrainInventoryLockingService[F](doobieInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
      liveOrderService = LiveOrderService[F](doobieOrderRepository)
      liveOrderLifecycleApplicationService =
        LiveOrderLifecycleApplicationService[F](
          orderService = liveOrderService,
          orderRepository = doobieOrderRepository,
          reservationLifecycle = reservationLifecycle
        )
      liveManagerService = LiveManagerService[F](doobieManagerRepository)
      liveFlightBookingApplicationService =
        LiveFlightBookingApplicationService[F](
          flightService = liveFlightService,
          flightRepository = doobieFlightRepository,
          flightInventoryLockingService = liveFlightInventoryLockingService,
          orderRepository = doobieOrderRepository,
          travelerProfileRepository = doobieTravelerProfileRepository
        )
      liveHotelBookingApplicationService =
        LiveHotelBookingApplicationService[F](
        hotelService = liveHotelService,
        hotelRepository = doobieHotelRepository,
        orderRepository = doobieOrderRepository,
        travelerProfileRepository = doobieTravelerProfileRepository,
        hotelInventoryLockingService = liveHotelInventoryLockingService
      )
      liveTrainBookingApplicationService =
        LiveTrainBookingApplicationService[F](
          trainService = liveTrainService,
          trainRepository = doobieTrainRepository,
          trainInventoryLockingService = liveTrainInventoryLockingService,
          orderRepository = doobieOrderRepository,
          travelerProfileRepository = doobieTravelerProfileRepository
        )
      liveAttractionBookingApplicationService =
        LiveAttractionBookingApplicationService[F](
          attractionRepository = doobieAttractionRepository,
          ticketEligibilityService = liveTicketEligibilityService,
          orderRepository = doobieOrderRepository,
          orderService = liveOrderService,
          travelerProfileRepository = doobieTravelerProfileRepository
        )
      liveTourGroupApplicationService =
        LiveTourGroupApplicationService[F](
          tourGroupRepository = doobieTourGroupRepository,
          travelerProfileRepository = doobieTravelerProfileRepository,
          orderService = liveOrderService,
          orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
          orderRepository = doobieOrderRepository,
          flightBookingApplicationService = liveFlightBookingApplicationService,
          hotelBookingApplicationService = liveHotelBookingApplicationService,
          trainBookingApplicationService = liveTrainBookingApplicationService,
          attractionBookingApplicationService = liveAttractionBookingApplicationService
        )
      liveTrainAdminApplicationService =
        LiveTrainAdminApplicationService[F](
          trainService = liveTrainService,
          trainRepository = doobieTrainRepository
        )
      liveAttractionAdminApplicationService =
        LiveAttractionAdminApplicationService[F](
          managerService = liveManagerService,
          attractionRepository = doobieAttractionRepository
        )
      liveManagerWorkflowApplicationService =
        LiveManagerWorkflowApplicationService[F](
          managerService = liveManagerService,
          managerRepository = doobieManagerRepository,
          orderRepository = doobieOrderRepository,
          orderService = liveOrderService,
          reservationLifecycle = reservationLifecycle,
          flightRepository = doobieFlightRepository,
          hotelRepository = doobieHotelRepository,
          attractionRepository = doobieAttractionRepository
        )
      liveAvatarApplicationService =
        LiveAvatarApplicationService[F](
          userService = liveUserService,
          avatarStorage = localAvatarStorage
        )
      apiRouter =
        ApiRouter[F](
          userService = liveUserService,
          travelerProfileService = liveTravelerProfileService,
          orderService = liveOrderService,
          orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
          flightBookingApplicationService = liveFlightBookingApplicationService,
          hotelBookingApplicationService = liveHotelBookingApplicationService,
          trainBookingApplicationService = liveTrainBookingApplicationService,
          attractionBookingApplicationService = liveAttractionBookingApplicationService,
          tourGroupApplicationService = liveTourGroupApplicationService,
          trainAdminApplicationService = liveTrainAdminApplicationService,
          attractionAdminApplicationService = liveAttractionAdminApplicationService,
          managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
          avatarApplicationService = liveAvatarApplicationService,
          userRepository = doobieUserRepository,
          travelerProfileRepository = doobieTravelerProfileRepository,
          orderRepository = doobieOrderRepository,
          inventoryReservationRepository = doobieInventoryReservationRepository,
          avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
          frontendDistRootDirectoryPath = frontendDistRootDirectoryPath
        )
    yield ApplicationWiring(
      userService = liveUserService,
      travelerProfileService = liveTravelerProfileService,
      flightService = liveFlightService,
      hotelService = liveHotelService,
      trainService = liveTrainService,
      ticketEligibilityService = liveTicketEligibilityService,
      flightInventoryLockingService = liveFlightInventoryLockingService,
      hotelInventoryLockingService = liveHotelInventoryLockingService,
      trainInventoryLockingService = liveTrainInventoryLockingService,
      orderService = liveOrderService,
      orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
      managerService = liveManagerService,
      flightBookingApplicationService = liveFlightBookingApplicationService,
      hotelBookingApplicationService = liveHotelBookingApplicationService,
      trainBookingApplicationService = liveTrainBookingApplicationService,
      attractionBookingApplicationService = liveAttractionBookingApplicationService,
      tourGroupApplicationService = liveTourGroupApplicationService,
      trainAdminApplicationService = liveTrainAdminApplicationService,
      attractionAdminApplicationService = liveAttractionAdminApplicationService,
      managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
      avatarApplicationService = liveAvatarApplicationService,
      userRepository = doobieUserRepository,
      travelerProfileRepository = doobieTravelerProfileRepository,
      flightRepository = doobieFlightRepository,
      hotelRepository = doobieHotelRepository,
      trainRepository = doobieTrainRepository,
      attractionRepository = doobieAttractionRepository,
      tourGroupRepository = doobieTourGroupRepository,
      orderRepository = doobieOrderRepository,
      inventoryReservationRepository = doobieInventoryReservationRepository,
      managerRepository = doobieManagerRepository,
      httpApp = apiRouter.routes.orNotFound
    )
