// OrderPlannerPlainSql 封装订单模块的plain SQL 实现。

package com.typesafe.travel.persistence.order

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import io.circe.Json
import io.circe.parser.parse

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Duration, Instant}
import java.util.UUID

object OrderPlannerPlainSql:
  def listByUser(connection: Connection, input: ListOrdersPlannerRequest): IO[OrderListPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, Instant.now()) *> IO.blocking {
      PlainSqlSupport.withStatement(connection, orderSelectSql + " where buyer_user_id = ? order by created_at desc") { statement =>
        statement.setString(1, input.userId)
        OrderListPlannerResponse(PlainSqlSupport.queryList(statement)(resultSet => readOrder(connection, resultSet)))
      }
    }

  def create(connection: Connection, input: CreateOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, now) *> IO.blocking {
      val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, orderId)
        statement.setString(2, input.ownerUserId)
        statement.setString(3, "PendingSelection")
        statement.setString(4, "Draft")
        statement.setString(5, input.orderCurrency)
        statement.setBigDecimal(6, java.math.BigDecimal.ZERO)
        statement.setBigDecimal(7, java.math.BigDecimal.ZERO)
        statement.setTimestamp(8, Timestamp.from(now))
        statement.executeUpdate()
      }
      findRequired(connection, orderId)
    }

  def get(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, Instant.now()) *> IO.blocking(findRequired(connection, input.orderId))

  def submit(connection: Connection, input: OrderIdPlannerRequest): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, Instant.now()) *> validateOrderCanTransitionToPayment(connection, input.orderId) *> updateStatus(connection, input.orderId, "PendingPayment", None, None)

  def pay(connection: Connection, input: PayOrderPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, now) *>
      IO.blocking {
        if input.paymentSucceeded then
          val order = findRequired(connection, input.orderId)
          if !isOrderPayable(order.status) then
            throw new IllegalArgumentException(s"Order '${input.orderId}' cannot accept payments while in status ${order.status}")
          val hasFlightLineItem = order.orderLineItems.exists(_.orderItemKind == "Flight")
          if hasFlightLineItem then
            val travelerIds = input.travelerIds.getOrElse(throw new IllegalArgumentException(s"Flight order '${input.orderId}' requires traveler selection"))
            updateFlightTravelerSelection(connection, input.orderId, travelerIds)
          AttractionPlannerPlainSql.consumeInventoryForPaidOrder(connection, input.orderId)
          val resolvedOrderTotalAmount = resolveOrderTotalAmount(connection, input.orderId, BigDecimal(order.totalPrice))
          val paymentId = s"payment-${UUID.randomUUID().toString.take(12)}"
          PlainSqlSupport.withStatement(connection, "insert into order_payments(payment_id, order_id, payment_amount, payment_currency, payment_method, payment_status, authorized_at, created_at, captured_at, metadata_json) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
            statement.setString(1, paymentId)
            statement.setString(2, input.orderId)
            statement.setBigDecimal(3, resolvedOrderTotalAmount.bigDecimal)
            statement.setString(4, order.orderCurrency)
            statement.setString(5, input.paymentMethod)
            statement.setString(6, "Captured")
            statement.setTimestamp(7, Timestamp.from(now))
            statement.setTimestamp(8, Timestamp.from(now))
            statement.setTimestamp(9, Timestamp.from(now))
            statement.setString(10, "{}")
            statement.executeUpdate()
          }
          refreshOrderTotals(connection, input.orderId, resolvedOrderTotalAmount)
          PlainSqlSupport.withStatement(connection, "update orders set status = ?, paid_at = ?, confirmed_at = ? where order_id = ?") { statement =>
            statement.setString(1, "Confirmed")
            statement.setTimestamp(2, Timestamp.from(now))
            statement.setTimestamp(3, Timestamp.from(now))
            statement.setString(4, input.orderId)
            statement.executeUpdate()
          }
        findRequired(connection, input.orderId)
      }

  private def updateFlightTravelerSelection(connection: Connection, orderId: String, travelerIds: List[String]): Unit =
    val cleanedTravelerIds = travelerIds.map(_.trim).filter(_.nonEmpty).distinct
    if cleanedTravelerIds.isEmpty then
      throw new IllegalArgumentException("At least one traveler is required to pay a flight order")

    PlainSqlSupport.withStatement(connection, "select order_item_id, snapshot_json from order_line_items where order_id = ? and item_kind = ? order by sort_index limit 1") { statement =>
      statement.setString(1, orderId)
      statement.setString(2, "Flight")
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          val orderItemId = resultSet.getString("order_item_id")
          val snapshotJson = Option(resultSet.getString("snapshot_json")).getOrElse("{}")
          val nextSnapshotJson = parse(snapshotJson).getOrElse(Json.obj()).mapObject { jsonObject =>
            jsonObject.add("travelerIds", Json.fromValues(cleanedTravelerIds.map(Json.fromString)))
          }.noSpaces
          PlainSqlSupport.withStatement(connection, "update order_line_items set snapshot_json = ?, traveler_ids_json = ? where order_item_id = ?") { updateStatement =>
            updateStatement.setString(1, nextSnapshotJson)
            updateStatement.setString(2, Json.fromValues(cleanedTravelerIds.map(Json.fromString)).noSpaces)
            updateStatement.setString(3, orderItemId)
            updateStatement.executeUpdate()
          }
        else throw new IllegalArgumentException(s"Flight order item for order '$orderId' was not found")
      finally resultSet.close()
    }

  def cancel(connection: Connection, input: OrderIdPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, now) *>
      IO.blocking(findRequired(connection, input.orderId)).flatMap { orderBeforeCancel =>
        updateStatus(connection, input.orderId, "Cancelled", Some("cancelled_at"), Some(now)).flatTap { _ =>
          PlainSqlSupport.withStatement(connection, "update orders set remaining_refundable_amount = 0 where order_id = ?") { statement =>
            statement.setString(1, input.orderId)
            statement.executeUpdate()
          }
          releaseTrainSeatAllocations(connection, input.orderId)
          if orderBeforeCancel.status == "Confirmed" then IO.blocking(AttractionPlannerPlainSql.restoreInventoryForCancelledOrder(connection, input.orderId))
          else IO.unit
        }
      }

  def requestRefund(connection: Connection, input: RequestRefundPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, now) *> IO.blocking {
      val order = findRequired(connection, input.orderId)
      val refundId = s"refund-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into order_refunds(refund_id, order_id, refund_amount, refund_currency, refund_reason, refund_status, requested_at, created_at, metadata_json) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, refundId)
        statement.setString(2, input.orderId)
        statement.setBigDecimal(3, BigDecimal(order.remainingRefundableAmount).bigDecimal)
        statement.setString(4, order.orderCurrency)
        statement.setString(5, input.refundReason)
        statement.setString(6, "Requested")
        statement.setTimestamp(7, Timestamp.from(now))
        statement.setTimestamp(8, Timestamp.from(now))
        statement.setString(9, "{}")
        statement.executeUpdate()
      }
      findRequired(connection, input.orderId)
    }

  def approveRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, now) *> refundStatus(connection, input, "Approved", Some("approved_at"), now)

  def settleRefund(connection: Connection, input: RefundDecisionPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    expireTrainOrdersIfNeeded(connection, now) *> refundStatus(connection, input, "Settled", Some("settled_at"), now)

  private def updateStatus(connection: Connection, orderId: String, status: String, timestampColumn: Option[String], timestamp: Option[Instant]): IO[OrderPlannerResponse] =
    IO.blocking {
      val sql = timestampColumn match
        case Some(column) => s"update orders set status = ?, $column = ? where order_id = ?"
        case None => "update orders set status = ? where order_id = ?"
      PlainSqlSupport.withStatement(connection, sql) { statement =>
        statement.setString(1, status)
        timestampColumn match
          case Some(_) =>
            statement.setTimestamp(2, Timestamp.from(timestamp.getOrElse(Instant.now())))
            statement.setString(3, orderId)
          case None =>
            statement.setString(2, orderId)
        statement.executeUpdate()
      }
      findRequired(connection, orderId)
    }

  private def refundStatus(connection: Connection, input: RefundDecisionPlannerRequest, status: String, timestampColumn: Option[String], now: Instant): IO[OrderPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, s"update order_refunds set refund_status = ?, ${timestampColumn.get} = ? where order_id = ? and refund_id = ?") { statement =>
        statement.setString(1, status)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setString(3, input.orderId)
        statement.setString(4, input.refundId)
        statement.executeUpdate()
      }
      findRequired(connection, input.orderId)
    }

  private val orderSelectSql =
    """
      select order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount,
             created_at, paid_at, confirmed_at, completed_at, cancelled_at
      from orders
    """

  private def findRequired(connection: Connection, orderId: String): OrderPlannerResponse =
    PlainSqlSupport.withStatement(connection, orderSelectSql + " where order_id = ?") { statement =>
      statement.setString(1, orderId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readOrder(connection, resultSet) else throw new IllegalArgumentException(s"Order '$orderId' was not found")
      finally resultSet.close()
    }

  private def isOrderPayable(orderStatus: String): Boolean =
    val normalizedStatus = orderStatus.trim
    normalizedStatus == "Draft" || normalizedStatus == "PendingSelection" || normalizedStatus == "PendingPayment"

  private def validateOrderCanTransitionToPayment(connection: Connection, orderId: String): IO[Unit] =
    IO.blocking {
      val order = findRequired(connection, orderId)
      if !isOrderPayable(order.status) then
        throw new IllegalArgumentException(s"Order '${orderId}' cannot accept payments while in status ${order.status}")
      ()
    }

  private def releaseTrainSeatAllocations(connection: Connection, orderId: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "delete from train_seat_allocations where order_id = ?") { statement =>
        statement.setString(1, orderId)
        statement.executeUpdate()
      }
      ()
    }

  private def expireTrainOrdersIfNeeded(connection: Connection, now: Instant): IO[Unit] =
    IO.blocking {
      val cutoff = Timestamp.from(now.minus(Duration.ofMinutes(10)))
      val expiredOrderIds = PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.created_at
          from orders o
          where o.order_type in (?, ?)
            and o.status in (?, ?, ?)
            and o.created_at <= ?
            and exists (
              select 1
              from order_line_items li
              where li.order_id = o.order_id and li.item_kind = ?
            )
          order by o.created_at, o.order_id
        """
      ) { statement =>
        statement.setString(1, "PendingSelection")
        statement.setString(2, "TrainBooking")
        statement.setString(3, "Draft")
        statement.setString(4, "PendingSelection")
        statement.setString(5, "PendingPayment")
        statement.setTimestamp(6, cutoff)
        statement.setString(7, "Train")

        val resultSet = statement.executeQuery()
        try {
          val ids = List.newBuilder[String]
          while resultSet.next() do {
            ids += resultSet.getString("order_id")
          }
          ids.result().distinct
        } finally {
          resultSet.close()
        }
      }

      expiredOrderIds.foreach { orderId =>
        PlainSqlSupport.withStatement(connection, "update orders set status = ?, cancelled_at = coalesce(cancelled_at, ?) where order_id = ? and status in (?, ?, ?)") { statement =>
          statement.setString(1, "Cancelled")
          statement.setTimestamp(2, Timestamp.from(now))
          statement.setString(3, orderId)
          statement.setString(4, "Draft")
          statement.setString(5, "PendingSelection")
          statement.setString(6, "PendingPayment")
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "update orders set remaining_refundable_amount = 0 where order_id = ?") { statement =>
          statement.setString(1, orderId)
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "update order_line_items set item_status = ? where order_id = ?") { statement =>
          statement.setString(1, "Cancelled")
          statement.setString(2, orderId)
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "delete from train_seat_allocations where order_id = ?") { statement =>
          statement.setString(1, orderId)
          statement.executeUpdate()
        }
      }
      ()
    }

  private def readOrder(connection: Connection, resultSet: ResultSet): OrderPlannerResponse =
    val orderId = resultSet.getString("order_id")
    val storedTotalPrice = resultSet.getBigDecimal("total_price_amount")
    val resolvedTotalPrice = resolveOrderTotalAmount(connection, orderId, BigDecimal(storedTotalPrice))
    val payments = readPayments(connection, orderId)
    val refunds = readRefunds(connection, orderId)
    val capturedAmount = payments.filter(_.paymentStatus == "Captured").map(p => BigDecimal(p.paymentAmount)).sum
    val resolvedCapturedAmount =
      if capturedAmount > BigDecimal(0) then capturedAmount
      else if resultSet.getString("status") == "Confirmed" then resolvedTotalPrice
      else capturedAmount
    OrderPlannerResponse(
      orderId = orderId,
      buyerUserId = resultSet.getString("buyer_user_id"),
      orderType = resultSet.getString("order_type"),
      status = resultSet.getString("status"),
      orderCurrency = resultSet.getString("currency"),
      totalPrice = resolvedTotalPrice.toString,
      totalCapturedAmount = resolvedCapturedAmount.toString,
      totalSettledRefundAmount = refunds.filter(_.refundStatus == "Settled").map(r => BigDecimal(r.refundAmount)).sum.toString,
      remainingRefundableAmount = resolvedTotalPrice.toString,
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      paidAt = Option(resultSet.getTimestamp("paid_at")).map(_.toInstant.toString),
      confirmedAt = Option(resultSet.getTimestamp("confirmed_at")).map(_.toInstant.toString),
      completedAt = Option(resultSet.getTimestamp("completed_at")).map(_.toInstant.toString),
      cancelledAt = Option(resultSet.getTimestamp("cancelled_at")).map(_.toInstant.toString),
      orderLineItems = readLineItems(connection, orderId),
      orderPayments = payments,
      orderRefunds = refunds
    )

  private def readLineItems(connection: Connection, orderId: String): List[OrderLineItemPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select li.order_item_id, li.item_kind, li.item_status, li.supplier_review_status, li.booked_amount, li.booked_currency, li.snapshot_json,
               li.room_type_id, rt.name as room_type_name, rt.hotel_id, h.name as hotel_name, h.location as hotel_location,
               li.train_id, li.train_from_stop_id, li.train_to_stop_id, li.train_seat_inventory_id, li.seat_class, li.traveler_ids_json, li.unit_amount, li.unit_currency,
               t.train_number,
               fs.station_code as train_from_station_code, fs.station_name as train_from_station_name, fs.departure_time as train_departure_time,
               ts.station_code as train_to_station_code, ts.station_name as train_to_station_name, ts.arrival_time as train_arrival_time
        from order_line_items li
        left join hotel_room_types rt on rt.room_type_id = li.room_type_id
        left join hotels h on h.hotel_id = rt.hotel_id
        left join trains t on t.train_id = li.train_id
        left join train_stops fs on fs.stop_id = li.train_from_stop_id
        left join train_stops ts on ts.stop_id = li.train_to_stop_id
        where li.order_id = ?
        order by li.sort_index
      """
    ) { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        val itemKind = resultSet.getString("item_kind")
        val travelerIdsJson = Option(resultSet.getString("traveler_ids_json")).flatMap(value => parse(value).toOption).getOrElse(Json.arr())
        val travelerCount = travelerIdsJson.asArray.map(_.size).getOrElse(0)
        val trainPricing =
          if itemKind == "Train" then resolveTrainPricing(resultSet.getString("train_id"), resultSet.getString("train_from_stop_id"), resultSet.getString("train_to_stop_id"), resultSet.getString("seat_class"), travelerCount, connection)
          else None
        val bookedAmount =
          if itemKind == "Train" then trainPricing.map(_.totalPrice).getOrElse(resultSet.getBigDecimal("booked_amount"))
          else resultSet.getBigDecimal("booked_amount")
        OrderLineItemPlannerResponse(
          orderItemId = resultSet.getString("order_item_id"),
          orderItemKind = itemKind,
          orderItemStatus = resultSet.getString("item_status"),
          supplierReviewStatus = resultSet.getString("supplier_review_status"),
          bookedAmount = bookedAmount.toString,
          bookedCurrency = resultSet.getString("booked_currency"),
          summaryLabel =
            if itemKind == "Hotel" then enrichHotelSummaryLabel(resultSet)
            else if itemKind == "Train" then enrichTrainSummaryLabel(resultSet, trainPricing)
            else Option(resultSet.getString("snapshot_json")).getOrElse(itemKind)
        )
    }
  }

  private def resolveOrderTotalAmount(connection: Connection, orderId: String, fallbackAmount: BigDecimal): BigDecimal =
    val lineItemAmount = readLineItems(connection, orderId).map(lineItem => BigDecimal(lineItem.bookedAmount)).sum
    if lineItemAmount > BigDecimal(0) then lineItemAmount else fallbackAmount

  private def refreshOrderTotals(connection: Connection, orderId: String, totalAmount: BigDecimal): Unit =
    PlainSqlSupport.withStatement(connection, "update orders set total_price_amount = ?, remaining_refundable_amount = ? where order_id = ?") { statement =>
      statement.setBigDecimal(1, totalAmount.bigDecimal)
      statement.setBigDecimal(2, totalAmount.bigDecimal)
      statement.setString(3, orderId)
      statement.executeUpdate()
    }

  private def readPayments(connection: Connection, orderId: String): List[PaymentPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select payment_id, payment_amount, payment_currency, payment_method, payment_status, coalesce(created_at, authorized_at) as authorized_at, captured_at from order_payments where order_id = ? order by authorized_at") { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        PaymentPlannerResponse(
          resultSet.getString("payment_id"),
          resultSet.getBigDecimal("payment_amount").toString,
          resultSet.getString("payment_currency"),
          resultSet.getString("payment_method"),
          resultSet.getString("payment_status"),
          resultSet.getTimestamp("authorized_at").toInstant.toString,
          Option(resultSet.getTimestamp("captured_at")).map(_.toInstant.toString)
        )
      }
    }

  private def readRefunds(connection: Connection, orderId: String): List[RefundPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select refund_id, refund_amount, refund_currency, refund_reason, refund_status, coalesce(created_at, requested_at) as requested_at, approved_at, settled_at from order_refunds where order_id = ? order by requested_at") { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        RefundPlannerResponse(
          resultSet.getString("refund_id"),
          resultSet.getBigDecimal("refund_amount").toString,
          resultSet.getString("refund_currency"),
          resultSet.getString("refund_reason"),
          resultSet.getString("refund_status"),
          resultSet.getTimestamp("requested_at").toInstant.toString,
          Option(resultSet.getTimestamp("approved_at")).map(_.toInstant.toString),
          Option(resultSet.getTimestamp("settled_at")).map(_.toInstant.toString)
        )
      }
    }

  private def enrichHotelSummaryLabel(resultSet: ResultSet): String =
    val snapshotJson = Option(resultSet.getString("snapshot_json")).getOrElse("{}")
    val baseJson = parse(snapshotJson).getOrElse(Json.obj())
    baseJson
      .mapObject { jsonObject =>
        val withHotelId = addStringField(jsonObject, "hotelId", resultSet.getString("hotel_id"))
        val withHotelName = addStringField(withHotelId, "hotelName", resultSet.getString("hotel_name"))
        val withHotelLocation = addStringField(withHotelName, "hotelLocation", resultSet.getString("hotel_location"))
        val withRoomTypeId = addStringField(withHotelLocation, "roomTypeId", resultSet.getString("room_type_id"))
        val withRoomTypeName = addStringField(withRoomTypeId, "roomTypeName", resultSet.getString("room_type_name"))
        withRoomTypeName
      }
      .noSpaces

  private def enrichTrainSummaryLabel(resultSet: ResultSet, trainPricing: Option[TrainOrderPricing]): String =
    val snapshotJson = Option(resultSet.getString("snapshot_json")).getOrElse("{}")
    val baseJson = parse(snapshotJson).getOrElse(Json.obj())
    val travelerIdsJson = Option(resultSet.getString("traveler_ids_json")).flatMap(value => parse(value).toOption).getOrElse(Json.arr())
    val resolvedPricing = trainPricing.getOrElse(
      TrainOrderPricing(
        unitPrice = Option(resultSet.getBigDecimal("unit_amount")).getOrElse(resultSet.getBigDecimal("booked_amount")),
        totalPrice = resultSet.getBigDecimal("booked_amount"),
        currency = Option(resultSet.getString("unit_currency")).orElse(Option(resultSet.getString("booked_currency"))).getOrElse("CNY")
      )
    )
    baseJson
      .mapObject { jsonObject =>
        val withTrainId = addStringField(jsonObject, "trainId", resultSet.getString("train_id"))
        val withTrainNumber = addStringField(withTrainId, "trainNumber", resultSet.getString("train_number"))
        val withFromStationCode = addStringField(withTrainNumber, "departureStationCode", resultSet.getString("train_from_station_code"))
        val withFromStationName = addStringField(withFromStationCode, "departureStation", resultSet.getString("train_from_station_name"))
        val withToStationCode = addStringField(withFromStationName, "arrivalStationCode", resultSet.getString("train_to_station_code"))
        val withToStationName = addStringField(withToStationCode, "arrivalStation", resultSet.getString("train_to_station_name"))
        val withDepartureTime = addStringField(withToStationName, "departureTime", Option(resultSet.getTimestamp("train_departure_time")).map(_.toInstant.toString).orNull)
        val withArrivalTime = addStringField(withDepartureTime, "arrivalTime", Option(resultSet.getTimestamp("train_arrival_time")).map(_.toInstant.toString).orNull)
        val withSeatClass = addStringField(withArrivalTime, "seatClass", resultSet.getString("seat_class"))
        val withTravelerIds = withSeatClass.add("travelerIds", travelerIdsJson)
        val withUnitPrice = addStringField(withTravelerIds, "unitPrice", resolvedPricing.unitPrice.toString)
        val withCurrency = addStringField(withUnitPrice, "currency", resolvedPricing.currency)
        val withTotalPrice = addStringField(withCurrency, "totalPrice", resolvedPricing.totalPrice.toString)
        withTotalPrice
      }
      .noSpaces

  private final case class TrainOrderPricing(unitPrice: BigDecimal, totalPrice: BigDecimal, currency: String)

  private def resolveTrainPricing(trainId: String, fromStopId: String, toStopId: String, seatClass: String, travelerCount: Int, connection: Connection): Option[TrainOrderPricing] =
    if List(trainId, fromStopId, toStopId, seatClass).exists(value => value == null || value.trim.isEmpty) then None
    else
      val orderedStops = PlainSqlSupport.withStatement(connection, "select stop_id, sequence_no from train_stops where train_id = ? order by sequence_no asc") { statement =>
        statement.setString(1, trainId)
        PlainSqlSupport.queryList(statement) { resultSet =>
          (resultSet.getString("stop_id"), resultSet.getInt("sequence_no"))
        }
      }.sortBy(_._2)

      val fromSequenceNo = orderedStops.collectFirst { case (stopId, sequenceNo) if stopId == fromStopId => sequenceNo }.getOrElse(return None)
      val toSequenceNo = orderedStops.collectFirst { case (stopId, sequenceNo) if stopId == toStopId => sequenceNo }.getOrElse(return None)
      if toSequenceNo <= fromSequenceNo then None
      else
        val routeStops = orderedStops.filter { case (_, sequenceNo) => sequenceNo >= fromSequenceNo && sequenceNo <= toSequenceNo }
        val segmentPriceByStopPair = PlainSqlSupport.withStatement(
          connection,
          "select from_stop_id, to_stop_id, amount, currency from train_segment_prices where train_id = ? and seat_class = ?"
        ) { statement =>
          statement.setString(1, trainId)
          statement.setString(2, seatClass)
          PlainSqlSupport.queryList(statement) { resultSet =>
            ((resultSet.getString("from_stop_id"), resultSet.getString("to_stop_id")), (BigDecimal(resultSet.getBigDecimal("amount")), resultSet.getString("currency")))
          }.toMap
        }

        val routeSegmentPrices = routeStops.sliding(2).toList.flatMap {
          case List((leftStopId, _), (rightStopId, _)) => segmentPriceByStopPair.get((leftStopId, rightStopId))
          case _ => Nil
        }

        if routeSegmentPrices.isEmpty || routeSegmentPrices.size != routeStops.size - 1 then None
        else
          val unitPrice = routeSegmentPrices.foldLeft(BigDecimal(0)) { case (acc, (segmentPrice, _)) => acc + segmentPrice }
          val currency = routeSegmentPrices.headOption.map(_._2).getOrElse("CNY")
          Some(TrainOrderPricing(unitPrice = unitPrice, totalPrice = unitPrice * BigDecimal(travelerCount.max(1)), currency = currency))

  private def addStringField(jsonObject: io.circe.JsonObject, fieldName: String, fieldValue: String | Null): io.circe.JsonObject =
    Option(fieldValue).map(_.trim).filter(_.nonEmpty) match
      case Some(value) => jsonObject.add(fieldName, Json.fromString(value))
      case None        => jsonObject
