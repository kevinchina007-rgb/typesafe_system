package com.typesafe.travel.api

import com.typesafe.travel.api.application.*
import cats.effect.IO
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
import com.typesafe.travel.persistence.advertising.*
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
  def resource: Resource[IO, ApplicationWiring] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")
    DatabaseTransactor.resource[IO](databaseConfig).evalMap { databaseTransactor =>
      for
        _ <- SchemaInitializer.initialize(databaseTransactor)
        doobieUploadedBinaryAssetRepository = DoobieUploadedBinaryAssetRepository[IO](databaseTransactor)
        doobieUserRepository = DoobieUserRepository[IO](databaseTransactor)
        doobieAuthRepository = DoobieAuthRepository[IO](databaseTransactor)
        doobieTravelerProfileRepository = DoobieTravelerProfileRepository[IO](databaseTransactor)
        doobieFlightRepository = DoobieFlightRepository[IO](databaseTransactor)
        doobieHotelRepository = DoobieHotelRepository[IO](databaseTransactor)
        doobieTrainRepository = DoobieTrainRepository[IO](databaseTransactor)
        doobieAttractionRepository = DoobieAttractionRepository[IO](databaseTransactor)
        doobieBlogRepository = DoobieBlogRepository[IO](databaseTransactor)
        doobieReviewRepository = DoobieReviewRepository[IO](databaseTransactor)
        doobieFeedbackRepository = DoobieFeedbackRepository[IO](databaseTransactor)
        doobieAdvertisementRepository = DoobieAdvertisementRepository[IO](databaseTransactor)
        doobieTourGroupRepository = DoobieTourGroupRepository[IO](databaseTransactor)
        doobieOrderRepository = DoobieOrderRepository[IO](databaseTransactor)
        doobieInventoryReservationRepository = DoobieInventoryReservationRepository[IO](databaseTransactor)
        doobieManagerRepository = DoobieManagerRepository[IO](databaseTransactor)
        databaseAvatarStorage = DatabaseAvatarStorage.create[IO](doobieUploadedBinaryAssetRepository)
        databaseContentImageStorage = DatabaseContentImageStorage.create[IO](doobieUploadedBinaryAssetRepository)
        databaseTourGroupChatAttachmentStorage = DatabaseTourGroupChatAttachmentStorage.create[IO](doobieUploadedBinaryAssetRepository)
        liveUserService = LiveUserService[IO](doobieUserRepository)
        liveTravelerProfileService =
          LiveTravelerProfileService[IO](
            doobieTravelerProfileRepository,
            doobieUserRepository,
            () => Clock[IO].realTimeInstant.map(_.atZone(java.time.ZoneId.systemDefault()).toLocalDate)
          )
        liveFlightService = LiveFlightService[IO](doobieFlightRepository)
        liveHotelService = LiveHotelService[IO](doobieHotelRepository)
        liveTrainService = TrainService[IO](doobieTrainRepository)
        liveTicketEligibilityService = TicketEligibilityService[IO]()
        reservationLifecycle = LiveReservationLifecycle[IO](doobieInventoryReservationRepository)
        liveFlightInventoryLockingService =
          LiveFlightInventoryLockingService[IO](doobieInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
        liveHotelInventoryLockingService =
          LiveHotelInventoryLockingService[IO](doobieInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
        liveTrainInventoryLockingService =
          LiveTrainInventoryLockingService[IO](doobieInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
        liveOrderService = LiveOrderService[IO](doobieOrderRepository)
        liveOrderLifecycleApplicationService =
          LiveOrderLifecycleApplicationService[IO](
            orderService = liveOrderService,
            orderRepository = doobieOrderRepository,
            reservationLifecycle = reservationLifecycle
          )
        liveManagerService = LiveManagerService[IO](doobieManagerRepository)
        liveAuthApplicationService =
          LiveAuthApplicationService[IO](
            authRepository = doobieAuthRepository,
            userService = liveUserService,
            userRepository = doobieUserRepository,
            managerService = liveManagerService,
            trainRepository = doobieTrainRepository
          )
        liveFlightBookingApplicationService =
          new FlightBookingApplicationService(
            flightService = liveFlightService,
            flightRepository = doobieFlightRepository,
            flightInventoryLockingService = liveFlightInventoryLockingService,
            orderRepository = doobieOrderRepository,
            travelerProfileRepository = doobieTravelerProfileRepository
          )
        liveHotelBookingApplicationService =
          LiveHotelBookingApplicationService[IO](
            hotelService = liveHotelService,
            hotelRepository = doobieHotelRepository,
            orderRepository = doobieOrderRepository,
            travelerProfileRepository = doobieTravelerProfileRepository,
            hotelInventoryLockingService = liveHotelInventoryLockingService
          )
        liveTrainBookingApplicationService =
          LiveTrainBookingApplicationService[IO](
            trainService = liveTrainService,
            trainRepository = doobieTrainRepository,
            trainInventoryLockingService = liveTrainInventoryLockingService,
            orderRepository = doobieOrderRepository,
            travelerProfileRepository = doobieTravelerProfileRepository
          )
        liveAttractionBookingApplicationService =
          LiveAttractionBookingApplicationService[IO](
            attractionRepository = doobieAttractionRepository,
            ticketEligibilityService = liveTicketEligibilityService,
            orderRepository = doobieOrderRepository,
            orderService = liveOrderService,
            travelerProfileRepository = doobieTravelerProfileRepository
          )
        liveBlogApplicationService =
          LiveBlogApplicationService[IO](
            blogRepository = doobieBlogRepository,
            userRepository = doobieUserRepository,
            contentImageStorage = databaseContentImageStorage
          )
        liveReviewApplicationService =
          LiveReviewApplicationService[IO](
            reviewRepository = doobieReviewRepository,
            orderRepository = doobieOrderRepository,
            userRepository = doobieUserRepository,
            contentImageStorage = databaseContentImageStorage
          )
        liveFeedbackApplicationService =
          LiveFeedbackApplicationService[IO](
            feedbackRepository = doobieFeedbackRepository,
            reviewRepository = doobieReviewRepository,
            userRepository = doobieUserRepository
          )
        liveAdvertisementApplicationService =
          LiveAdvertisementApplicationService[IO](
            advertisementRepository = doobieAdvertisementRepository,
            hotelRepository = doobieHotelRepository,
            attractionRepository = doobieAttractionRepository
          )
        liveTourGroupApplicationService =
          LiveTourGroupApplicationService[IO](
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
            chatAttachmentStorage = databaseTourGroupChatAttachmentStorage
          )
        liveTrainAdminApplicationService =
          LiveTrainAdminApplicationService[IO](
            trainService = liveTrainService,
            trainRepository = doobieTrainRepository
          )
        liveAttractionAdminApplicationService =
          LiveAttractionAdminApplicationService[IO](
            managerService = liveManagerService,
            attractionRepository = doobieAttractionRepository
          )
        liveManagerWorkflowApplicationService =
          LiveManagerWorkflowApplicationService[IO](
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
          LiveAvatarApplicationService[IO](
            userService = liveUserService,
            avatarStorage = databaseAvatarStorage
          )
        apiRouter =
          ApiRouter[IO](
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
            feedbackApplicationService = liveFeedbackApplicationService,
            advertisementApplicationService = liveAdvertisementApplicationService,
            tourGroupApplicationService = liveTourGroupApplicationService,
            trainAdminApplicationService = liveTrainAdminApplicationService,
            attractionAdminApplicationService = liveAttractionAdminApplicationService,
            managerWorkflowApplicationService = liveManagerWorkflowApplicationService,
            avatarApplicationService = liveAvatarApplicationService,
            userRepository = doobieUserRepository,
            travelerProfileRepository = doobieTravelerProfileRepository,
            orderRepository = doobieOrderRepository,
            inventoryReservationRepository = doobieInventoryReservationRepository,
            uploadedBinaryAssetRepository = Some(doobieUploadedBinaryAssetRepository),
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
        feedbackApplicationService = liveFeedbackApplicationService,
        advertisementApplicationService = liveAdvertisementApplicationService,
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
        feedbackRepository = doobieFeedbackRepository,
        advertisementRepository = doobieAdvertisementRepository,
        authRepository = doobieAuthRepository,
        tourGroupRepository = doobieTourGroupRepository,
        orderRepository = doobieOrderRepository,
        inventoryReservationRepository = doobieInventoryReservationRepository,
        managerRepository = doobieManagerRepository,
        httpApp = apiRouter.routes.orNotFound
      )
    }

