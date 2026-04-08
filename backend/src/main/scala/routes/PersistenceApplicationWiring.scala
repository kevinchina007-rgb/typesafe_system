package com.typesafe.travel.api

import com.typesafe.travel.api.application.*
import cats.effect.kernel.Async
import cats.effect.kernel.Clock
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.*
import com.typesafe.travel.api.memory.*
import com.typesafe.travel.api.storage.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.attraction.*
import com.typesafe.travel.persistence.auth.*
import com.typesafe.travel.persistence.content.*
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

import ApplicationWiringPaths.resolveConfiguredPath

object PersistenceApplicationWiring:
  def resource[F[_]: Async]: Resource[F, ApplicationWiring[F]] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)
    val localContentImageStorage = LocalContentImageStorage.create[F](contentUploadRootDirectoryPath)
    val localTourGroupChatAttachmentStorage = LocalTourGroupChatAttachmentStorage.create[F](contentUploadRootDirectoryPath)

    DatabaseTransactor.resource[F](databaseConfig).evalMap { databaseTransactor =>
      for
        _ <- SchemaInitializer.initialize(databaseTransactor)
        doobieUserRepository = DoobieUserRepository[F](databaseTransactor)
        doobieAuthRepository = DoobieAuthRepository[F](databaseTransactor)
        doobieTravelerProfileRepository = DoobieTravelerProfileRepository[F](databaseTransactor)
        doobieFlightRepository = DoobieFlightRepository[F](databaseTransactor)
        doobieHotelRepository = DoobieHotelRepository[F](databaseTransactor)
        doobieTrainRepository = DoobieTrainRepository[F](databaseTransactor)
        doobieAttractionRepository = DoobieAttractionRepository[F](databaseTransactor)
        doobieBlogRepository = DoobieBlogRepository[F](databaseTransactor)
        doobieReviewRepository = DoobieReviewRepository[F](databaseTransactor)
        doobieTourGroupRepository = DoobieTourGroupRepository[F](databaseTransactor)
        doobieOrderRepository = DoobieOrderRepository[F](databaseTransactor)
        doobieInventoryReservationRepository = DoobieInventoryReservationRepository[F](databaseTransactor)
        doobieManagerRepository = DoobieManagerRepository[F](databaseTransactor)
        liveUserService = LiveUserService[F](doobieUserRepository)
        liveTravelerProfileService =
          LiveTravelerProfileService[F](
            doobieTravelerProfileRepository,
            doobieUserRepository,
            () => Clock[F].realTimeInstant.map(_.atZone(java.time.ZoneId.systemDefault()).toLocalDate)
          )
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
        liveAuthApplicationService =
          LiveAuthApplicationService[F](
            authRepository = doobieAuthRepository,
            userService = liveUserService,
            userRepository = doobieUserRepository,
            managerService = liveManagerService,
            trainRepository = doobieTrainRepository
          )
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
        liveBlogApplicationService =
          LiveBlogApplicationService[F](
            blogRepository = doobieBlogRepository,
            userRepository = doobieUserRepository,
            contentImageStorage = localContentImageStorage
          )
        liveReviewApplicationService =
          LiveReviewApplicationService[F](
            reviewRepository = doobieReviewRepository,
            orderRepository = doobieOrderRepository,
            userRepository = doobieUserRepository,
            contentImageStorage = localContentImageStorage
          )
        liveTourGroupApplicationService =
          LiveTourGroupApplicationService[F](
            tourGroupRepository = doobieTourGroupRepository,
            userRepository = doobieUserRepository,
            travelerProfileRepository = doobieTravelerProfileRepository,
            orderService = liveOrderService,
            orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
            orderRepository = doobieOrderRepository,
            flightBookingApplicationService = liveFlightBookingApplicationService,
            hotelBookingApplicationService = liveHotelBookingApplicationService,
            trainBookingApplicationService = liveTrainBookingApplicationService,
            attractionBookingApplicationService = liveAttractionBookingApplicationService,
            chatAttachmentStorage = localTourGroupChatAttachmentStorage
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
            authApplicationService = liveAuthApplicationService,
            flightBookingApplicationService = liveFlightBookingApplicationService,
            hotelBookingApplicationService = liveHotelBookingApplicationService,
            trainBookingApplicationService = liveTrainBookingApplicationService,
            attractionBookingApplicationService = liveAttractionBookingApplicationService,
            blogApplicationService = liveBlogApplicationService,
            reviewApplicationService = liveReviewApplicationService,
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
            contentUploadRootDirectoryPath = contentUploadRootDirectoryPath,
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
        authApplicationService = liveAuthApplicationService,
        managerService = liveManagerService,
        flightBookingApplicationService = liveFlightBookingApplicationService,
        hotelBookingApplicationService = liveHotelBookingApplicationService,
        trainBookingApplicationService = liveTrainBookingApplicationService,
        attractionBookingApplicationService = liveAttractionBookingApplicationService,
        blogApplicationService = liveBlogApplicationService,
        reviewApplicationService = liveReviewApplicationService,
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
        blogRepository = doobieBlogRepository,
        reviewRepository = doobieReviewRepository,
        authRepository = doobieAuthRepository,
        tourGroupRepository = doobieTourGroupRepository,
        orderRepository = doobieOrderRepository,
        inventoryReservationRepository = doobieInventoryReservationRepository,
        managerRepository = doobieManagerRepository,
        httpApp = apiRouter.routes.orNotFound
      )
    }
