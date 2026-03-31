package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

trait OrderLifecycleApplicationService[F[_]]:
  def listOrdersForUser(ownerUserId: UserId, currentTime: Instant): F[List[Order]]
  def getOrder(orderId: OrderId, currentTime: Instant): F[Order]
  def payOrder(orderId: OrderId, paymentMethod: PaymentMethod, paymentSucceeded: Boolean, currentTime: Instant): F[Order]
  def cancelOrder(orderId: OrderId, currentTime: Instant): F[Order]

final class LiveOrderLifecycleApplicationService[F[_]: MonadThrow](
    orderService: OrderService[F],
    orderRepository: OrderRepository[F],
    reservationLifecycle: ReservationLifecycle[F]
) extends OrderLifecycleApplicationService[F]:
  override def listOrdersForUser(ownerUserId: UserId, currentTime: Instant): F[List[Order]] =
    for
      orders <- orderService.listOrdersForUser(ownerUserId)
      _ <- orders.traverse_(order => reservationLifecycle.expireReservationsForOrder(order.orderId, currentTime).void)
      refreshedOrders <- orderService.listOrdersForUser(ownerUserId)
    yield refreshedOrders

  override def getOrder(orderId: OrderId, currentTime: Instant): F[Order] =
    for
      _ <- reservationLifecycle.expireReservationsForOrder(orderId, currentTime)
      order <- loadOrder(orderId)
    yield order

  override def payOrder(orderId: OrderId, paymentMethod: PaymentMethod, paymentSucceeded: Boolean, currentTime: Instant): F[Order] =
    if paymentSucceeded then
      for
        _ <- reservationLifecycle.expireReservationsForOrder(orderId, currentTime)
        paidOrder <- orderService.payOrder(orderId, paymentMethod, currentTime)
        _ <- reservationLifecycle.confirmReservationsForOrder(orderId, currentTime)
        order <- loadOrder(orderId)
      yield order
    else
      for
        _ <- reservationLifecycle.expireReservationsForOrder(orderId, currentTime)
        order <- loadOrder(orderId)
      yield order

  override def cancelOrder(orderId: OrderId, currentTime: Instant): F[Order] =
    for
      order <- loadOrder(orderId)
      cancelledOrder <- order.cancelDraftOrder(currentTime).liftTo[F]
      _ <- orderRepository.saveOrder(cancelledOrder)
      _ <- reservationLifecycle.releaseReservationsForOrder(orderId, currentTime)
      refreshedOrder <- loadOrder(orderId)
    yield refreshedOrder

  private def loadOrder(orderId: OrderId): F[Order] =
    orderRepository.findOrderById(orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
