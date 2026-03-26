package com.typesafe.travel.api

import com.typesafe.travel.api.application.*
import cats.effect.kernel.Async
import com.typesafe.travel.api.memory.*
import com.typesafe.travel.api.storage.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.order.domain.*
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
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    avatarApplicationService: AvatarApplicationService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    flightRepository: FlightRepository[F],
    hotelRepository: HotelRepository[F],
    orderRepository: OrderRepository[F],
    httpApp: HttpApp[F]
)

object ApplicationWiring:
  def create[F[_]: Async]: ApplicationWiring[F] =
    val inMemoryUserRepository = InMemoryUserRepository.create[F]
    val inMemoryTravelerProfileRepository = InMemoryTravelerProfileRepository.create[F]
    val inMemoryFlightRepository = InMemoryFlightRepository.create[F]
    val inMemoryHotelRepository = InMemoryHotelRepository.create[F]
    val inMemoryOrderRepository = InMemoryOrderRepository.create[F]
    val avatarUploadRootDirectoryPath = Paths.get("uploads", "avatars").toAbsolutePath.normalize()
    val localAvatarStorage = LocalAvatarStorage.create[F](avatarUploadRootDirectoryPath)

    val liveUserService = LiveUserService[F](inMemoryUserRepository)
    val liveTravelerProfileService =
      LiveTravelerProfileService[F](inMemoryTravelerProfileRepository, inMemoryUserRepository)
    val liveFlightService = LiveFlightService[F](inMemoryFlightRepository)
    val liveHotelService = LiveHotelService[F](inMemoryHotelRepository)
    val liveOrderService = LiveOrderService[F](inMemoryOrderRepository)
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
      flightBookingApplicationService = liveFlightBookingApplicationService,
      hotelBookingApplicationService = liveHotelBookingApplicationService,
      avatarApplicationService = liveAvatarApplicationService,
      userRepository = inMemoryUserRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository,
      flightRepository = inMemoryFlightRepository,
      hotelRepository = inMemoryHotelRepository,
      orderRepository = inMemoryOrderRepository,
      httpApp = apiRouter.routes.orNotFound
    )
