// 本文件封装 BookHotelPlanner 对应的 plain SQL 实现。
package com.typesafe.travel.hotel.tables

// 这个文件只负责酒店预订链路的后端数据库动作，例如校验房态、写入订单项、扣减可用房间和落库存。
// 前端不会镜像它，因为这些步骤是业务执行过程，不是跨端契约；前端只需要知道预订请求长什么样、返回订单什么状态。
// 将预订 SQL 独立出来，可以让 planner 保持薄层，同时避免把库存写入细节泄漏到 UI 层。

import com.typesafe.travel.hotel.objects.*
import io.circe.Json

import java.sql.{Connection, Timestamp}
import java.time.Instant

object BookHotelPlannerPlainSql:
  def insertOrder(
      connection: Connection,
      orderId: String,
      buyerUserId: String,
      totalPriceAmount: java.math.BigDecimal,
      currency: String,
      now: Instant
  ): Unit =
    val orderStatement = connection.prepareStatement(
      "insert into orders(order_id, buyer_user_id, order_type, status, currency, total_price_amount, remaining_refundable_amount, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)"
    )
    try
      orderStatement.setString(1, orderId)
      orderStatement.setString(2, buyerUserId)
      orderStatement.setString(3, "Hotel")
      orderStatement.setString(4, "Draft")
      orderStatement.setString(5, currency)
      orderStatement.setBigDecimal(6, totalPriceAmount)
      orderStatement.setBigDecimal(7, totalPriceAmount)
      orderStatement.setTimestamp(8, Timestamp.from(now))
      orderStatement.executeUpdate()
    finally orderStatement.close()

  def insertOrderItem(
      connection: Connection,
      orderItemId: String,
      orderId: String,
      hotel: Hotel,
      roomType: RoomType,
      request: BookHotelPlannerRequest,
      totalPriceAmount: String,
      currency: String
  ): Unit =
    val itemStatement = connection.prepareStatement(
      "insert into order_line_items(order_item_id, order_id, item_kind, item_status, booked_amount, booked_currency, snapshot_json, sort_index) values (?, ?, ?, ?, ?, ?, ?, ?)"
    )
    try
      itemStatement.setString(1, orderItemId)
      itemStatement.setString(2, orderId)
      itemStatement.setString(3, "Hotel")
      itemStatement.setString(4, "Active")
      itemStatement.setBigDecimal(5, new java.math.BigDecimal(totalPriceAmount))
      itemStatement.setString(6, currency)
      itemStatement.setString(
        7,
        Json
          .obj(
            "hotelId" -> Json.fromString(hotel.hotelId.value),
            "hotelName" -> Json.fromString(hotel.hotelName.value),
            "hotelLocation" -> Json.fromString(hotel.hotelLocation.value),
            "roomTypeId" -> Json.fromString(roomType.roomTypeId.value),
            "roomTypeName" -> Json.fromString(roomType.roomTypeName.value),
            "roomName" -> Json.fromString(roomType.roomTypeName.value),
            "guestTravelerIds" -> Json.fromValues(request.guestTravelerIds.map(Json.fromString)),
            "checkInDate" -> Json.fromString(request.checkInDate),
            "checkOutDate" -> Json.fromString(request.checkOutDate),
            "roomCount" -> Json.fromInt(request.roomCount),
            "unitPriceAmount" -> Json.fromBigDecimal(roomType.basePrice.amount),
            "unitPriceCurrency" -> Json.fromString(roomType.basePrice.currency.toString)
          )
          .noSpaces
      )
      itemStatement.setInt(8, 0)
      itemStatement.executeUpdate()
    finally itemStatement.close()
