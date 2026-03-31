package com.typesafe.travel.api.memory

import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*

final class InMemoryOrderRepository[F[_]: Sync] private (
    orderState: Ref[F, Map[OrderId, Order]],
    orderSequence: Ref[F, Long],
    orderItemSequence: Ref[F, Long],
    paymentSequence: Ref[F, Long],
    refundSequence: Ref[F, Long]
) extends OrderRepository[F]:
  override def nextOrderId: F[OrderId] =
    orderSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> OrderId(s"order-$nextValue")
    }

  override def nextOrderItemId: F[OrderItemId] =
    orderItemSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> OrderItemId(s"order-item-$nextValue")
    }

  override def nextPaymentId: F[PaymentId] =
    paymentSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> PaymentId(s"payment-$nextValue")
    }

  override def nextRefundId: F[RefundId] =
    refundSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> RefundId(s"refund-$nextValue")
    }

  override def findOrderById(orderId: OrderId): F[Option[Order]] =
    orderState.get.map(_.get(orderId))

  override def findOrderByOrderItemId(orderItemId: OrderItemId): F[Option[Order]] =
    orderState.get.map(_.values.find(_.orderLineItems.exists(_.orderItemId == orderItemId)))

  override def findAllOrders: F[List[Order]] =
    orderState.get.map(_.values.toList.sortBy(_.orderId.value))

  override def findOrdersByOwnerUserId(ownerUserId: UserId): F[List[Order]] =
    orderState.get.map(_.values.filter(_.ownerUserId == ownerUserId).toList.sortBy(_.orderId.value))

  override def saveOrder(order: Order): F[Order] =
    orderState.update(_ + (order.orderId -> order)).as(order)

  override def deleteOrder(orderId: OrderId): F[Unit] =
    orderState.update(_ - orderId)

object InMemoryOrderRepository:
  def create[F[_]: Sync]: InMemoryOrderRepository[F] =
    new InMemoryOrderRepository[F](
      orderState = Ref.unsafe[F, Map[OrderId, Order]](Map.empty),
      orderSequence = Ref.unsafe[F, Long](0L),
      orderItemSequence = Ref.unsafe[F, Long](0L),
      paymentSequence = Ref.unsafe[F, Long](0L),
      refundSequence = Ref.unsafe[F, Long](0L)
    )
