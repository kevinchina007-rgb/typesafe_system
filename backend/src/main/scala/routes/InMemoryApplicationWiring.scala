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

object InMemoryApplicationWiring:
  def create: ApplicationWiring =
    val inMemoryUserRepository = InMemoryUserRepository.create[IO]
    val inMemoryAuthRepository = InMemoryAuthRepository.create[IO]
    val inMemoryTravelerProfileRepository = InMemoryTravelerProfileRepository.create[IO]
    val inMemoryFlightRepository = InMemoryFlightRepository.create[IO]
    val inMemoryHotelRepository = InMemoryHotelRepository.create[IO]
    val inMemoryTrainRepository = InMemoryTrainRepository.create[IO]
    val inMemoryAttractionRepository = InMemoryAttractionRepository.create[IO]
    val inMemoryBlogRepository = InMemoryBlogRepository.create[IO]
    val inMemoryReviewRepository = InMemoryReviewRepository.create[IO]
    val inMemoryFeedbackRepository = InMemoryFeedbackRepository.create[IO]
    val inMemoryAdvertisementRepository = InMemoryAdvertisementRepository.create[IO]
    val inMemoryTourGroupRepository = InMemoryTourGroupRepository.create[IO]
    val inMemoryOrderRepository = InMemoryOrderRepository.create[IO]
    val inMemoryInventoryReservationRepository = InMemoryInventoryReservationRepository.create[IO]
    val inMemoryManagerRepository = InMemoryManagerRepository.create[IO]
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")
    val localAvatarStorage = LocalAvatarStorage.create[IO](avatarUploadRootDirectoryPath)
    val localContentImageStorage = LocalContentImageStorage.create[IO](contentUploadRootDirectoryPath)
    val localTourGroupChatAttachmentStorage = LocalTourGroupChatAttachmentStorage.create[IO](contentUploadRootDirectoryPath)

    val liveUserService = LiveUserService[IO](inMemoryUserRepository)
    val liveTravelerProfileService =
      LiveTravelerProfileService[IO](
        inMemoryTravelerProfileRepository,
        inMemoryUserRepository,
        () => Clock[IO].realTimeInstant.map(_.atZone(java.time.ZoneId.systemDefault()).toLocalDate)
      )
    val liveFlightService = LiveFlightService[IO](inMemoryFlightRepository)
    val liveHotelService = LiveHotelService[IO](inMemoryHotelRepository)
    val liveTrainService = TrainService[IO](inMemoryTrainRepository)
    val liveTicketEligibilityService = TicketEligibilityService[IO]()
    val reservationLifecycle = LiveReservationLifecycle[IO](inMemoryInventoryReservationRepository)
    val liveFlightInventoryLockingService =
      LiveFlightInventoryLockingService[IO](inMemoryInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
    val liveHotelInventoryLockingService =
      LiveHotelInventoryLockingService[IO](inMemoryInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
    val liveTrainInventoryLockingService =
      LiveTrainInventoryLockingService[IO](inMemoryInventoryReservationRepository, java.time.Duration.ofMinutes(15), reservationLifecycle)
    val liveOrderService = LiveOrderService[IO](inMemoryOrderRepository)
    val liveOrderLifecycleApplicationService =
        LiveOrderLifecycleApplicationService[IO](
          orderService = liveOrderService,
          orderRepository = inMemoryOrderRepository,
          reservationLifecycle = reservationLifecycle
        )
    val liveManagerService = LiveManagerService[IO](inMemoryManagerRepository)
    val liveAuthApplicationService =
      LiveAuthApplicationService[IO](
        authRepository = inMemoryAuthRepository,
        userService = liveUserService,
        userRepository = inMemoryUserRepository,
        managerService = liveManagerService,
        trainRepository = inMemoryTrainRepository
      )
    val liveFlightBookingApplicationService =
      new FlightBookingApplicationService(
        flightService = liveFlightService,
        flightRepository = inMemoryFlightRepository,
        flightInventoryLockingService = liveFlightInventoryLockingService,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveHotelBookingApplicationService =
      LiveHotelBookingApplicationService[IO](
        hotelService = liveHotelService,
        hotelRepository = inMemoryHotelRepository,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        hotelInventoryLockingService = liveHotelInventoryLockingService
      )
    val liveTrainBookingApplicationService =
      LiveTrainBookingApplicationService[IO](
        trainService = liveTrainService,
        trainRepository = inMemoryTrainRepository,
        trainInventoryLockingService = liveTrainInventoryLockingService,
        orderRepository = inMemoryOrderRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveAttractionBookingApplicationService =
      LiveAttractionBookingApplicationService[IO](
        attractionRepository = inMemoryAttractionRepository,
        ticketEligibilityService = liveTicketEligibilityService,
        orderRepository = inMemoryOrderRepository,
        orderService = liveOrderService,
        travelerProfileRepository = inMemoryTravelerProfileRepository
      )
    val liveBlogApplicationService =
      LiveBlogApplicationService[IO](
        blogRepository = inMemoryBlogRepository,
        userRepository = inMemoryUserRepository,
        contentImageStorage = localContentImageStorage
      )
    val liveReviewApplicationService =
      LiveReviewApplicationService[IO](
        reviewRepository = inMemoryReviewRepository,
        orderRepository = inMemoryOrderRepository,
        userRepository = inMemoryUserRepository,
        contentImageStorage = localContentImageStorage
      )
    val liveFeedbackApplicationService =
      LiveFeedbackApplicationService[IO](
        feedbackRepository = inMemoryFeedbackRepository,
        reviewRepository = inMemoryReviewRepository,
        userRepository = inMemoryUserRepository
      )
    val liveAdvertisementApplicationService =
      LiveAdvertisementApplicationService[IO](
        advertisementRepository = inMemoryAdvertisementRepository,
        hotelRepository = inMemoryHotelRepository,
        attractionRepository = inMemoryAttractionRepository
      )
    val liveTourGroupApplicationService =
      LiveTourGroupApplicationService[IO](
        tourGroupRepository = inMemoryTourGroupRepository,
        userRepository = inMemoryUserRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        orderService = liveOrderService,
        orderLifecycleApplicationService = liveOrderLifecycleApplicationService,
        orderRepository = inMemoryOrderRepository,
        flightBookingApplicationService = liveFlightBookingApplicationService,
        hotelBookingApplicationService = liveHotelBookingApplicationService,
        trainBookingApplicationService = liveTrainBookingApplicationService,
        attractionBookingApplicationService = liveAttractionBookingApplicationService,
        chatAttachmentStorage = localTourGroupChatAttachmentStorage
      )
    val liveTrainAdminApplicationService =
      LiveTrainAdminApplicationService[IO](
        trainService = liveTrainService,
        trainRepository = inMemoryTrainRepository
      )
    val liveAttractionAdminApplicationService =
      LiveAttractionAdminApplicationService[IO](
        managerService = liveManagerService,
        attractionRepository = inMemoryAttractionRepository
      )
    val liveManagerWorkflowApplicationService =
        LiveManagerWorkflowApplicationService[IO](
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
      LiveAvatarApplicationService[IO](
        userService = liveUserService,
        avatarStorage = localAvatarStorage
      )

    val apiRouter =
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
            userRepository = inMemoryUserRepository,
            travelerProfileRepository = inMemoryTravelerProfileRepository,
            orderRepository = inMemoryOrderRepository,
            inventoryReservationRepository = inMemoryInventoryReservationRepository,
            uploadedBinaryAssetRepository = None,
            avatarUploadRootDirectoryPath = avatarUploadRootDirectoryPath,
            contentUploadRootDirectoryPath = contentUploadRootDirectoryPath,
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
      userRepository = inMemoryUserRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository,
      flightRepository = inMemoryFlightRepository,
      hotelRepository = inMemoryHotelRepository,
      trainRepository = inMemoryTrainRepository,
      attractionRepository = inMemoryAttractionRepository,
      blogRepository = inMemoryBlogRepository,
      reviewRepository = inMemoryReviewRepository,
      feedbackRepository = inMemoryFeedbackRepository,
      advertisementRepository = inMemoryAdvertisementRepository,
      authRepository = inMemoryAuthRepository,
      tourGroupRepository = inMemoryTourGroupRepository,
      orderRepository = inMemoryOrderRepository,
      inventoryReservationRepository = inMemoryInventoryReservationRepository,
      managerRepository = inMemoryManagerRepository,
      httpApp = apiRouter.routes.orNotFound
    )


