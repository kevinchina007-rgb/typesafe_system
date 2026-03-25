package com.typesafe.travel.api

import cats.effect.kernel.Async
import com.typesafe.travel.api.memory.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.traveler.domain.*
import org.http4s.HttpApp
import org.http4s.implicits.*

final case class ApplicationWiring[F[_]](
    userService: UserService[F],
    travelerProfileService: TravelerProfileService[F],
    orderService: OrderService[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    orderRepository: OrderRepository[F],
    httpApp: HttpApp[F]
)

object ApplicationWiring:
  def create[F[_]: Async]: ApplicationWiring[F] =
    val inMemoryUserRepository = InMemoryUserRepository.create[F]
    val inMemoryTravelerProfileRepository = InMemoryTravelerProfileRepository.create[F]
    val inMemoryOrderRepository = InMemoryOrderRepository.create[F]

    val liveUserService = LiveUserService[F](inMemoryUserRepository)
    val liveTravelerProfileService =
      LiveTravelerProfileService[F](inMemoryTravelerProfileRepository, inMemoryUserRepository)
    val liveOrderService = LiveOrderService[F](inMemoryOrderRepository)

    val apiRouter =
      ApiRouter[F](
        userService = liveUserService,
        travelerProfileService = liveTravelerProfileService,
        orderService = liveOrderService,
        userRepository = inMemoryUserRepository,
        travelerProfileRepository = inMemoryTravelerProfileRepository,
        orderRepository = inMemoryOrderRepository
      )

    ApplicationWiring(
      userService = liveUserService,
      travelerProfileService = liveTravelerProfileService,
      orderService = liveOrderService,
      userRepository = inMemoryUserRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository,
      orderRepository = inMemoryOrderRepository,
      httpApp = apiRouter.routes.orNotFound
    )
