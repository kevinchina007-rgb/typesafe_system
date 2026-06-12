// TrainDatabaseCodecs 定义数据库 codec 映射。

package com.typesafe.travel.persistence.codecs

import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import java.time.Instant

object TrainDatabaseCodecs:
  final case class SerializedTrainBookingSnapshot(
      trainId: String,
      trainNumber: String,
      fromStopId: String,
      fromStopSequenceNo: Option[Int],
      fromStationCode: String,
      fromStationName: String,
      toStopId: String,
      toStopSequenceNo: Option[Int],
      toStationCode: String,
      toStationName: String,
      departureTime: String,
      arrivalTime: String,
      seatInventoryId: String,
      seatClass: String,
      requestedSeatPreference: Option[String],
      seatAssignments: Option[Vector[SerializedTrainSeatAssignment]],
      travelerIds: Vector[String],
      saleStartsAt: String,
      unitPriceAmount: BigDecimal,
      unitPriceCurrency: String
  )

  final case class SerializedTrainSeatAssignment(
      travelerId: String,
      seatId: String,
      carriageNo: Int,
      seatNo: String,
      seatLabel: String,
      seatPositionType: String
  )

  given Encoder[SerializedTrainBookingSnapshot] = deriveEncoder
  given Decoder[SerializedTrainBookingSnapshot] = Decoder.instance { cursor =>
    (for
      trainId <- cursor.downField("trainId").as[String]
      trainNumber <- cursor.downField("trainNumber").as[String]
      fromStationCode <- cursor.downField("fromStationCode").as[String]
        .orElse(cursor.downField("departureStationCode").as[String])
        .orElse(Right(trainId))
      fromStationName <- cursor.downField("fromStationName").as[String]
        .orElse(cursor.downField("departureStation").as[String])
        .orElse(Right(fromStationCode))
      toStationCode <- cursor.downField("toStationCode").as[String]
        .orElse(cursor.downField("arrivalStationCode").as[String])
        .orElse(Right(trainId))
      toStationName <- cursor.downField("toStationName").as[String]
        .orElse(cursor.downField("arrivalStation").as[String])
        .orElse(Right(toStationCode))
      departureTime <- cursor.downField("departureTime").as[String].orElse(Right(Instant.EPOCH.toString))
      arrivalTime <- cursor.downField("arrivalTime").as[String].orElse(Right(departureTime))
      seatClassText <- cursor.downField("seatClass").as[String]
      requestedSeatPreference <- cursor.downField("requestedSeatPreference").as[Option[String]]
      seatAssignments <- cursor.downField("seatAssignments").as[Option[Vector[SerializedTrainSeatAssignment]]]
      travelerIds <- cursor.downField("travelerIds").as[Option[Vector[String]]].map(_.getOrElse(Vector.empty))
      saleStartsAt <- cursor.downField("saleStartsAt").as[String].orElse(Right(departureTime))
      fromStopId <- cursor.downField("fromStopId").as[String]
        .orElse(cursor.downField("departureStationCode").as[String])
        .orElse(Right(fromStationCode))
      fromStopSequenceNo <- cursor.downField("fromStopSequenceNo").as[Option[Int]]
      toStopId <- cursor.downField("toStopId").as[String]
        .orElse(cursor.downField("arrivalStationCode").as[String])
        .orElse(Right(toStationCode))
      toStopSequenceNo <- cursor.downField("toStopSequenceNo").as[Option[Int]]
      seatInventoryId <- cursor.downField("seatInventoryId").as[String].orElse(Right(s"legacy-$seatClassText"))
      unitPriceAmount <- cursor.downField("unitPriceAmount").as[BigDecimal]
        .orElse(cursor.downField("unitPrice").as[BigDecimal])
        .orElse(cursor.downField("totalPriceAmount").as[BigDecimal])
        .orElse(cursor.downField("totalPrice").as[BigDecimal])
        .orElse(Right(BigDecimal(0)))
      unitPriceCurrency <- cursor.downField("unitPriceCurrency").as[String]
        .orElse(cursor.downField("currency").as[String])
        .orElse(cursor.downField("totalPriceCurrency").as[String])
        .orElse(Right("CNY"))
    yield SerializedTrainBookingSnapshot(
      trainId = trainId,
      trainNumber = trainNumber,
      fromStopId = fromStopId,
      fromStopSequenceNo = fromStopSequenceNo,
      fromStationCode = fromStationCode,
      fromStationName = fromStationName,
      toStopId = toStopId,
      toStopSequenceNo = toStopSequenceNo,
      toStationCode = toStationCode,
      toStationName = toStationName,
      departureTime = departureTime,
      arrivalTime = arrivalTime,
      seatInventoryId = seatInventoryId,
      seatClass = seatClassText,
      requestedSeatPreference = requestedSeatPreference,
      seatAssignments = seatAssignments,
      travelerIds = travelerIds,
      saleStartsAt = saleStartsAt,
      unitPriceAmount = unitPriceAmount,
      unitPriceCurrency = unitPriceCurrency
    ))
  }
  given Encoder[SerializedTrainSeatAssignment] = deriveEncoder
  given Decoder[SerializedTrainSeatAssignment] = deriveDecoder

  def encodeTrainOrderItemSnapshot(trainOrderItem: TrainOrderItem): String =
    SerializedTrainBookingSnapshot(
      trainId = trainOrderItem.trainBookingSnapshot.trainId.value,
      trainNumber = trainOrderItem.trainBookingSnapshot.trainNumber.value,
      fromStopId = trainOrderItem.trainBookingSnapshot.fromStopId.value,
      fromStopSequenceNo = Some(trainOrderItem.trainBookingSnapshot.fromStopSequenceNo),
      fromStationCode = trainOrderItem.trainBookingSnapshot.fromStationCode.value,
      fromStationName = trainOrderItem.trainBookingSnapshot.fromStationName.value,
      toStopId = trainOrderItem.trainBookingSnapshot.toStopId.value,
      toStopSequenceNo = Some(trainOrderItem.trainBookingSnapshot.toStopSequenceNo),
      toStationCode = trainOrderItem.trainBookingSnapshot.toStationCode.value,
      toStationName = trainOrderItem.trainBookingSnapshot.toStationName.value,
      departureTime = trainOrderItem.trainBookingSnapshot.departureTime.toString,
      arrivalTime = trainOrderItem.trainBookingSnapshot.arrivalTime.toString,
      seatInventoryId = trainOrderItem.trainBookingSnapshot.seatInventoryId.value,
      seatClass = trainOrderItem.trainBookingSnapshot.seatClass.value,
      requestedSeatPreference = trainOrderItem.trainBookingSnapshot.requestedSeatPreference.map(_.toString),
      seatAssignments = Some(trainOrderItem.trainBookingSnapshot.seatAssignments.map(assignment =>
        SerializedTrainSeatAssignment(
          travelerId = assignment.travelerId.value,
          seatId = assignment.seatId.value,
          carriageNo = assignment.carriageNo,
          seatNo = assignment.seatNo,
          seatLabel = assignment.seatLabel,
          seatPositionType = assignment.seatPositionType.toString
        )
      )),
      travelerIds = trainOrderItem.trainBookingSnapshot.travelerIds.map(_.value),
      saleStartsAt = trainOrderItem.trainBookingSnapshot.saleStartsAt.toString,
      unitPriceAmount = trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.amount,
      unitPriceCurrency = trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.currency.toString
    ).asJson.noSpaces

  def decodeTrainBookingSnapshot(serializedValue: String): Either[Throwable, TrainBookingSnapshot] =
    io.circe.parser.decode[SerializedTrainBookingSnapshot](serializedValue).flatMap { serializedTrainBookingSnapshot =>
      for
        trainNumber <- TrainNumber.create(serializedTrainBookingSnapshot.trainNumber)
        fromStationCode <- TrainStationCode.create(serializedTrainBookingSnapshot.fromStationCode)
        fromStationName <- TrainStationName.create(serializedTrainBookingSnapshot.fromStationName)
        toStationCode <- TrainStationCode.create(serializedTrainBookingSnapshot.toStationCode)
        toStationName <- TrainStationName.create(serializedTrainBookingSnapshot.toStationName)
        seatClass <- TrainSeatClass.create(serializedTrainBookingSnapshot.seatClass)
        unitPriceCurrency <- DatabaseCodecs.parseCurrency(serializedTrainBookingSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedTrainBookingSnapshot.unitPriceAmount, unitPriceCurrency)
      yield TrainBookingSnapshot(
        trainId = TrainId(serializedTrainBookingSnapshot.trainId),
        trainNumber = trainNumber,
        fromStopId = TrainStopId(serializedTrainBookingSnapshot.fromStopId),
        fromStopSequenceNo = serializedTrainBookingSnapshot.fromStopSequenceNo.getOrElse(0),
        fromStationCode = fromStationCode,
        fromStationName = fromStationName,
        toStopId = TrainStopId(serializedTrainBookingSnapshot.toStopId),
        toStopSequenceNo = serializedTrainBookingSnapshot.toStopSequenceNo.getOrElse(1),
        toStationCode = toStationCode,
        toStationName = toStationName,
        departureTime = Instant.parse(serializedTrainBookingSnapshot.departureTime),
        arrivalTime = Instant.parse(serializedTrainBookingSnapshot.arrivalTime),
        seatInventoryId = TrainSeatInventoryId(serializedTrainBookingSnapshot.seatInventoryId),
        seatClass = seatClass,
        requestedSeatPreference = serializedTrainBookingSnapshot.requestedSeatPreference.map(TrainSeatPreference.fromText),
        seatAssignments = serializedTrainBookingSnapshot.seatAssignments.getOrElse(Vector.empty).map(assignment =>
          TrainTravelerSeatAssignment(
            travelerId = TravelerId(assignment.travelerId),
            seatId = TrainSeatId(assignment.seatId),
            carriageNo = assignment.carriageNo,
            seatNo = assignment.seatNo,
            seatLabel = assignment.seatLabel,
            seatPositionType = TrainSeatPositionType.fromText(assignment.seatPositionType)
          )
        ),
        travelerIds = serializedTrainBookingSnapshot.travelerIds.map(TravelerId.apply),
        saleStartsAt = Instant.parse(serializedTrainBookingSnapshot.saleStartsAt),
        unitPriceSnapshot = unitPriceSnapshot
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode train booking snapshot: ${error.getMessage}", error))
