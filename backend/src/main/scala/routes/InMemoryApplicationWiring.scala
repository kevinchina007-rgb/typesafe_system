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

object InMemoryApplicationWiring:
  def create[F[_]: Async]: ApplicationWiring[F] =
    val inMemoryUserRepository = InMemoryUserRepository.create[F]
    val inMemoryAuthRepository = InMemoryAuthRepository.create[F]
    val inMemoryTravelerProfileRepository = InMemoryTravelerProfileRepository.create[F]
    val inMemoryFlightRepository = InMemoryFlightRepository.create[F]
    val inMemoryHotelRepository = InMemoryHotelRepository.create[F]
    val inMemoryTrainRepository = InMemoryTrainRepository.create[F]
    val inMemoryAttractionRepository = InMemoryAttractionRepository.create[F]
    val inMemoryBlogRepository = InMemoryBlogRepository.create[F]
    val inMemoryReviewRepository = InMemoryReviewRepository.create[F]
    val inMemoryTourGroupRepository = InMemoryTourGroupRepository.create[F]
    val inMemoryOrderRepository = InMemoryOrderRepository.create[F]
    val inMemoryInventoryReservationRepository = InMemoryInventoryReservationRepository.create[F]
    val inMemoryManagerRepository = InMemoryManagerRepository.create[F]
    val avatarUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_AVATAR_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "avatars")
    val contentUploadRootDirectoryPath = resolveConfiguredPath("TRAVEL_CONTENT_UPLOAD_ROOT", "TRAVEL_UPLOAD_ROOT", "uploads", "content")
    val frontendDistRootDirectoryPath = resolveConfiguredPath("TRAVEL_FRONTEND_DIST_ROOT", "TRAVEL_STATIC_ROOT", "..", "frontend", "dist")
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)
    val localContentImageStorage = LocalContentImageStorage.create[F](contentUploadRootDirectoryPath)
    val localTourGroupChatAttachmentStorage = LocalTourGroupChatAttachmentStorage.create[F](contentUploadRootDirectoryPath)

    val liveUserService = LiveUserService[F](inMemoryUserRepository)
    val liveTravelerProfileService =
      LiveTravelerProfileService[F](
        inMemoryTravelerProfileRepository,
        inMemoryUserRepository,
        () => Clock[F].realTimeInstant.map(_.atZone(java.time.ZoneId.systemDefault()).toLocalDate)
      )
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
    val liveAuthApplicationService =
      LiveAuthApplicationService[F](
        authRepository = inMemoryAuthRepository,
        userService = liveUserService,
        userRepository = inMemoryUserRepository,
        managerService = liveManagerService,
        trainRepository = inMemoryTrainRepository
      )
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
    val liveBlogApplicationService =
      LiveBlogApplicationService[F](
        blogRepository = inMemoryBlogRepository,
        userRepository = inMemoryUserRepository,
        contentImageStorage = localContentImageStorage
      )
    val liveReviewApplicationService =
      LiveReviewApplicationService[F](
        reviewRepository = inMemoryReviewRepository,
        orderRepository = inMemoryOrderRepository,
        userRepository = inMemoryUserRepository,
        contentImageStorage = localContentImageStorage
      )
    val liveTourGroupApplicationService =
      LiveTourGroupApplicationService[F](
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
            userRepository = inMemoryUserRepository,
            travelerProfileRepository = inMemoryTravelerProfileRepository,
            orderRepository = inMemoryOrderRepository,
            inventoryReservationRepository = inMemoryInventoryReservationRepository,
            uploadedBinaryAssetReader = None,
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
      authRepository = inMemoryAuthRepository,
      tourGroupRepository = inMemoryTourGroupRepository,
      orderRepository = inMemoryOrderRepository,
      inventoryReservationRepository = inMemoryInventoryReservationRepository,
      managerRepository = inMemoryManagerRepository,
      httpApp = apiRouter.routes.orNotFound
    )

