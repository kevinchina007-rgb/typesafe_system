package com.typesafe.travel.api

import com.typesafe.travel.api.application.*
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.memory.*
import com.typesafe.travel.api.storage.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.flight.*
import com.typesafe.travel.persistence.hotel.*
import com.typesafe.travel.persistence.identity.*
import com.typesafe.travel.persistence.operations.*
import com.typesafe.travel.persistence.order.*
import com.typesafe.travel.persistence.traveler.*
import com.typesafe.travel.traveler.domain.*
import org.http4s.HttpApp
import org.http4s.implicits.*
import java.nio.file.Paths

final case class ApplicationWiring[F[_]](
    userService: UserService[F],
    travelerProfileService: TravelerProfileService[F],
    flightService: FlightService[F],
    hotelService: HotelService[F],
    orderService: OrderService[F],
    managerService: ManagerService[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    managerWorkflowApplicationService: ManagerWorkflowApplicationService[F],
    avatarApplicationService: AvatarApplicationService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    flightRepository: FlightRepository[F],
    hotelRepository: HotelRepository[F],
    orderRepository: OrderRepository[F],
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
    val inMemoryOrderRepository = InMemoryOrderRepository.create[F]
    val inMemoryManagerRepository = InMemoryManagerRepository.create[F]
    val avatarUploadRootDirectoryPath = Paths.get("uploads", "avatars").toAbsolutePath.normalize()
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)

    val liveUserService = LiveUserService[F](inMemoryUserRepository)
    val liveTravelerProfileService =
      LiveTravelerProfileService[F](inMemoryTravelerProfileRepository, inMemoryUserRepository)
    val liveFlightService = LiveFlightService[F](inMemoryFlightRepository)
    val liveHotelService = LiveHotelService[F](inMemoryHotelRepository)
    val liveOrderService = LiveOrderService[F](inMemoryOrderRepository)
    val liveManagerService = LiveManagerService[F](inMemoryManagerRepository)
    val liveFlightBookingApplicationService =
      LiveFlightBookingApplicationService[F](
        flightService = liveFlightService,
        flightRepository = inMemoryFlightRepository,
        orderService = liveOrderService,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveHotelBookingApplicationService =
      LiveHotelBookingApplicationService[F](
        hotelService = liveHotelService,
        hotelRepository = inMemoryHotelRepository,
        orderService = liveOrderService,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveManagerWorkflowApplicationService =
      LiveManagerWorkflowApplicationService[F](
        managerService = liveManagerService,
        managerRepository = inMemoryManagerRepository,
        orderRepository = inMemoryOrderRepository,
        orderService = liveOrderService,
        flightRepository = inMemoryFlightRepository,
        hotelRepository = inMemoryHotelRepository
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
        flightBookingApplicationService = liveFlightBookingApplicationService,
        hotelBookingApplicationService = liveHotelBookingApplicationService,
        managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
        avatarApplicationService = liveAvatarApplicationService,
        userRepository = inMemoryUserRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        orderRepository = inMemoryOrderRepository,
        avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath
      )

    ApplicationWiring(
      userService = liveUserService,
      travelerProfileService = liveTravelerProfileService,
      flightService = liveFlightService,
      hotelService = liveHotelService,
      orderService = liveOrderService,
      managerService = liveManagerService,
      flightBookingApplicationService = liveFlightBookingApplicationService,
      hotelBookingApplicationService = liveHotelBookingApplicationService,
      managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
      avatarApplicationService = liveAvatarApplicationService,
      userRepository = inMemoryUserRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository,
      flightRepository = inMemoryFlightRepository,
      hotelRepository = inMemoryHotelRepository,
      orderRepository = inMemoryOrderRepository,
      managerRepository = inMemoryManagerRepository,
      httpApp = apiRouter.routes.orNotFound
    )

  private def createPersistence[F[_]: Async]: F[ApplicationWiring[F]] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val databaseTransactor = DatabaseTransactor.create[F](databaseConfig)
    val avatarUploadRootDirectoryPath = Paths.get("uploads", "avatars").toAbsolutePath.normalize()
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)

    for
      _ <- SchemaInitializer.initialize(databaseTransactor)
      doobieUserRepository = DoobieUserRepository[F](databaseTransactor)
      doobieTravelerProfileRepository = DoobieTravelerProfileRepository[F](databaseTransactor)
      doobieFlightRepository = DoobieFlightRepository[F](databaseTransactor)
      doobieHotelRepository = DoobieHotelRepository[F](databaseTransactor)
      doobieOrderRepository = DoobieOrderRepository[F](databaseTransactor)
      doobieManagerRepository = DoobieManagerRepository[F](databaseTransactor)
      liveUserService = LiveUserService[F](doobieUserRepository)
      liveTravelerProfileService =
        LiveTravelerProfileService[F](doobieTravelerProfileRepository, doobieUserRepository)
      liveFlightService = LiveFlightService[F](doobieFlightRepository)
      liveHotelService = LiveHotelService[F](doobieHotelRepository)
      liveOrderService = LiveOrderService[F](doobieOrderRepository)
      liveManagerService = LiveManagerService[F](doobieManagerRepository)
      liveFlightBookingApplicationService =
        LiveFlightBookingApplicationService[F](
          flightService = liveFlightService,
          flightRepository = doobieFlightRepository,
          orderService = liveOrderService,
          orderRepository = doobieOrderRepository,
          travelerProfileRepository = doobieTravelerProfileRepository
        )
      liveHotelBookingApplicationService =
        LiveHotelBookingApplicationService[F](
          hotelService = liveHotelService,
          hotelRepository = doobieHotelRepository,
          orderService = liveOrderService,
          orderRepository = doobieOrderRepository,
          travelerProfileRepository = doobieTravelerProfileRepository
        )
      liveManagerWorkflowApplicationService =
        LiveManagerWorkflowApplicationService[F](
          managerService = liveManagerService,
          managerRepository = doobieManagerRepository,
          orderRepository = doobieOrderRepository,
          orderService = liveOrderService,
          flightRepository = doobieFlightRepository,
          hotelRepository = doobieHotelRepository
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
          flightBookingApplicationService = liveFlightBookingApplicationService,
          hotelBookingApplicationService = liveHotelBookingApplicationService,
          managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
          avatarApplicationService = liveAvatarApplicationService,
          userRepository = doobieUserRepository,
          travelerProfileRepository = doobieTravelerProfileRepository,
          orderRepository = doobieOrderRepository,
          avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath
        )
    yield ApplicationWiring(
      userService = liveUserService,
      travelerProfileService = liveTravelerProfileService,
      flightService = liveFlightService,
      hotelService = liveHotelService,
      orderService = liveOrderService,
      managerService = liveManagerService,
      flightBookingApplicationService = liveFlightBookingApplicationService,
      hotelBookingApplicationService = liveHotelBookingApplicationService,
      managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
      avatarApplicationService = liveAvatarApplicationService,
      userRepository = doobieUserRepository,
      travelerProfileRepository = doobieTravelerProfileRepository,
      flightRepository = doobieFlightRepository,
      hotelRepository = doobieHotelRepository,
      orderRepository = doobieOrderRepository,
      managerRepository = doobieManagerRepository,
      httpApp = apiRouter.routes.orNotFound
    )
