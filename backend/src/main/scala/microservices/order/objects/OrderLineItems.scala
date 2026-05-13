package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*

sealed trait OrderLineItem

object OrderLineItem:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[OrderLineItem] =
    Encoder.instance {
      case flightOrderItem: FlightOrderItem =>
        flightOrderItem.asJson.deepMerge(Json.obj("orderLineItemType" -> Json.fromString("flight")))
      case hotelOrderItem: HotelOrderItem =>
        hotelOrderItem.asJson.deepMerge(Json.obj("orderLineItemType" -> Json.fromString("hotel")))
      case trainOrderItem: TrainOrderItem =>
        trainOrderItem.asJson.deepMerge(Json.obj("orderLineItemType" -> Json.fromString("train")))
      case attractionOrderItem: AttractionOrderItem =>
        attractionOrderItem.asJson.deepMerge(Json.obj("orderLineItemType" -> Json.fromString("attraction")))
    }

  given sourceDecoder: Decoder[OrderLineItem] =
    Decoder.instance { cursor =>
      cursor.downField("orderLineItemType").as[String].flatMap {
        case "flight"     => cursor.as[FlightOrderItem]
        case "hotel"      => cursor.as[HotelOrderItem]
        case "train"      => cursor.as[TrainOrderItem]
        case "attraction" => cursor.as[AttractionOrderItem]
        case other        => Left(io.circe.DecodingFailure(s"Unknown orderLineItemType: $other", cursor.history))
      }
    }

final case class FlightOrderItem(
    orderItemId: OrderItemId,
    flightBookingSnapshot: FlightBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem

object FlightOrderItem:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[FlightOrderItem] = deriveEncoder[FlightOrderItem]
  given sourceDecoder: Decoder[FlightOrderItem] = deriveDecoder[FlightOrderItem]

final case class HotelOrderItem(
    orderItemId: OrderItemId,
    hotelBookingSnapshot: HotelBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem

object HotelOrderItem:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[HotelOrderItem] = deriveEncoder[HotelOrderItem]
  given sourceDecoder: Decoder[HotelOrderItem] = deriveDecoder[HotelOrderItem]

final case class TrainOrderItem(
    orderItemId: OrderItemId,
    trainBookingSnapshot: TrainBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem

object TrainOrderItem:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[TrainOrderItem] = deriveEncoder[TrainOrderItem]
  given sourceDecoder: Decoder[TrainOrderItem] = deriveDecoder[TrainOrderItem]

final case class AttractionOrderItem(
    orderItemId: OrderItemId,
    attractionTicketSnapshot: AttractionTicketSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem

object AttractionOrderItem:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[AttractionOrderItem] = deriveEncoder[AttractionOrderItem]
  given sourceDecoder: Decoder[AttractionOrderItem] = deriveDecoder[AttractionOrderItem]
