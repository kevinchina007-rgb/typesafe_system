package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*

trait OrderRepository[F[_]]:
  def nextOrderId: F[OrderId]
  def nextOrderItemId: F[OrderItemId]
  def nextPaymentId: F[PaymentId]
  def nextRefundId: F[RefundId]
  def findOrderById(orderId: OrderId): F[Option[Order]]
  def findOrderByOrderItemId(orderItemId: OrderItemId): F[Option[Order]]
  def findAllOrders: F[List[Order]]
  def findOrdersByOwnerUserId(ownerUserId: UserId): F[List[Order]]
  def saveOrder(order: Order): F[Order]
  def deleteOrder(orderId: OrderId): F[Unit]
