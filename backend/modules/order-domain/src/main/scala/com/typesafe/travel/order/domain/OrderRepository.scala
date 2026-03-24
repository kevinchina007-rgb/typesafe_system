package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*

trait OrderRepository[F[_]]:
  def nextOrderId: F[OrderId]
  def nextOrderItemId: F[OrderItemId]
  def findById(id: OrderId): F[Option[Order]]
  def findByUser(userId: UserId): F[List[Order]]
  def save(order: Order): F[Order]
