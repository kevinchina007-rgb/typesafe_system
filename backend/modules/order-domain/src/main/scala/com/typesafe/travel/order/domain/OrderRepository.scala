package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*

trait OrderRepository[F[_]]:
  def nextOrderId: F[OrderId]
  def nextOrderItemId: F[OrderItemId]
  def nextPaymentId: F[PaymentId]
  def nextRefundId: F[RefundId]
  def findOrderById(orderId: OrderId): F[Option[Order]]
  def findOrdersByOwnerUserId(ownerUserId: UserId): F[List[Order]]
  def saveOrder(order: Order): F[Order]
