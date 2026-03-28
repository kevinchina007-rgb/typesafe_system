package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryOrderRepository[F[_]: Sync] private (
    orderState: TrieMap[OrderId, Order],
    orderSequence: AtomicLong,
    orderItemSequence: AtomicLong,
    paymentSequence: AtomicLong,
    refundSequence: AtomicLong
) extends OrderRepository[F]:
  override def nextOrderId: F[OrderId] =
    Sync[F].delay(OrderId(s"order-${orderSequence.incrementAndGet()}"))

  override def nextOrderItemId: F[OrderItemId] =
    Sync[F].delay(OrderItemId(s"order-item-${orderItemSequence.incrementAndGet()}"))

  override def nextPaymentId: F[PaymentId] =
    Sync[F].delay(PaymentId(s"payment-${paymentSequence.incrementAndGet()}"))

  override def nextRefundId: F[RefundId] =
    Sync[F].delay(RefundId(s"refund-${refundSequence.incrementAndGet()}"))

  override def findOrderById(orderId: OrderId): F[Option[Order]] =
    Sync[F].delay(orderState.get(orderId))

  override def findOrderByOrderItemId(orderItemId: OrderItemId): F[Option[Order]] =
    Sync[F].delay(orderState.values.find(_.orderLineItems.exists(_.orderItemId == orderItemId)))

  override def findAllOrders: F[List[Order]] =
    Sync[F].delay(orderState.values.toList.sortBy(_.orderId.value))

  override def findOrdersByOwnerUserId(ownerUserId: UserId): F[List[Order]] =
    Sync[F].delay(orderState.values.filter(_.ownerUserId == ownerUserId).toList.sortBy(_.orderId.value))

  override def saveOrder(order: Order): F[Order] =
    Sync[F].delay {
      orderState.put(order.orderId, order)
      order
    }

  override def deleteOrder(orderId: OrderId): F[Unit] =
    Sync[F].delay {
      orderState.remove(orderId)
      ()
    }

object InMemoryOrderRepository:
  def create[F[_]: Sync]: InMemoryOrderRepository[F] =
    new InMemoryOrderRepository[F](
      orderState = TrieMap.empty,
      orderSequence = AtomicLong(0),
      orderItemSequence = AtomicLong(0),
      paymentSequence = AtomicLong(0),
      refundSequence = AtomicLong(0)
    )
