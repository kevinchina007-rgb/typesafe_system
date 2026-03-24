package com.typesafe.travel.order.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.Instant

trait OrderService[F[_]]:
  def createDraft(userId: UserId, createdAt: Instant): F[Order]
  def addFlightItem(orderId: OrderId, snapshot: FlightBookingSnapshot, totalPrice: Money): F[Order]
  def addHotelItem(orderId: OrderId, snapshot: HotelBookingSnapshot, totalPrice: Money): F[Order]
  def submitOrder(orderId: OrderId): F[Order]

final class LiveOrderService[F[_]: MonadThrow](
    repository: OrderRepository[F]
) extends OrderService[F]:

  override def createDraft(userId: UserId, createdAt: Instant): F[Order] =
    repository.nextOrderId.flatMap { orderId =>
      repository.save(Order.draft(orderId, userId, createdAt))
    }

  override def addFlightItem(
      orderId: OrderId,
      snapshot: FlightBookingSnapshot,
      totalPrice: Money
  ): F[Order] =
    addItem(
      orderId,
      id => FlightOrderItem(id, snapshot, totalPrice, OrderItemStatus.Reserved)
    )

  override def addHotelItem(
      orderId: OrderId,
      snapshot: HotelBookingSnapshot,
      totalPrice: Money
  ): F[Order] =
    addItem(
      orderId,
      id => HotelOrderItem(id, snapshot, totalPrice, OrderItemStatus.Reserved)
    )

  override def submitOrder(orderId: OrderId): F[Order] =
    loadOrder(orderId)
      .flatMap(order => MonadThrow[F].fromEither(order.submit))
      .flatMap(repository.save)

  private def addItem(
      orderId: OrderId,
      build: OrderItemId => OrderItem
  ): F[Order] =
    for
      order <- loadOrder(orderId)
      itemId <- repository.nextOrderItemId
      updated <- MonadThrow[F].fromEither(order.addItem(build(itemId)))
      saved <- repository.save(updated)
    yield saved

  private def loadOrder(orderId: OrderId): F[Order] =
    repository
      .findById(orderId)
      .flatMap(_.liftTo[F](OrderDomainError.OrderNotFound(orderId)))
