package com.typesafe.travel.train.domain

import munit.FunSuite

import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant}

final class TrainJourneySpec extends FunSuite:
  private val createdAt = Instant.parse("2026-03-28T08:00:00Z")
  private val departureAt = Instant.parse("2026-03-30T01:00:00Z")
  private val arrivalAt = Instant.parse("2026-03-30T05:00:00Z")

  test("quote sums adjacent segment prices for non-adjacent stops") {
    val trainJourney = sampleTrainJourney
    val quote = trainJourney.quote(
      fromStationCode = TrainStationCode.unsafe("SHA"),
      toStationCode = TrainStationCode.unsafe("HGH"),
      seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
      currentTime = createdAt.plusSeconds(3600)
    )

    assertEquals(quote.map(_.unitPrice.amount), Right(BigDecimal(180)))
  }

  test("quote rejects invalid station order") {
    val trainJourney = sampleTrainJourney
    val quote = trainJourney.quote(
      fromStationCode = TrainStationCode.unsafe("NKG"),
      toStationCode = TrainStationCode.unsafe("SHA"),
      seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
      currentTime = createdAt.plusSeconds(3600)
    )

    assert(quote.isLeft)
  }

  test("train cannot be booked before sale start") {
    val trainJourney = sampleTrainJourney
    val quote = trainJourney.quote(
      fromStationCode = TrainStationCode.unsafe("SHA"),
      toStationCode = TrainStationCode.unsafe("NKG"),
      seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
      currentTime = createdAt.minusSeconds(30)
    )

    assert(quote.left.exists(_.isInstanceOf[TrainError.TrainWasNotOpenForSale]))
  }

  test("refund policy uses departure offset windows") {
    val trainJourney = sampleTrainJourney
    val refundableMoney = trainJourney.calculateRefundAmount(
      departureTime = departureAt,
      ticketMoney = Money.unsafe(200, Currency.CNY),
      refundRequestedAt = departureAt.minus(Duration.ofHours(3))
    )

    assertEquals(refundableMoney.map(_.amount), Right(BigDecimal(100)))
  }

  private def sampleTrainJourney: TrainJourney =
    val trainId = TrainId("train-1")
    val managerId = ManagerId("manager-1")
    val inventoryId = TrainSeatInventoryId("inv-1")
    val shaStop = TrainStop(
      stopId = TrainStopId("stop-1"),
      stationCode = TrainStationCode.unsafe("SHA"),
      stationName = TrainStationName.unsafe("Shanghai"),
      sequenceNo = 0,
      arrivalTime = None,
      departureTime = Some(departureAt)
    )
    val nkgStop = TrainStop(
      stopId = TrainStopId("stop-2"),
      stationCode = TrainStationCode.unsafe("NKG"),
      stationName = TrainStationName.unsafe("Nanjing"),
      sequenceNo = 1,
      arrivalTime = Some(departureAt.plus(Duration.ofHours(1))),
      departureTime = Some(departureAt.plus(Duration.ofHours(1).plusMinutes(10)))
    )
    val hghStop = TrainStop(
      stopId = TrainStopId("stop-3"),
      stationCode = TrainStationCode.unsafe("HGH"),
      stationName = TrainStationName.unsafe("Hangzhou"),
      sequenceNo = 2,
      arrivalTime = Some(arrivalAt),
      departureTime = None
    )
    val seats =
      generateTrainSeats(
        trainId = trainId,
        inventory = TrainSeatInventory(
          inventoryId = inventoryId,
          trainId = trainId,
          seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
          totalSeats = SeatCount.unsafe(4),
          saleableSeats = SeatCount.unsafe(4),
          seatInventoryStatus = TrainSeatInventoryStatus.OpenForSale
        ),
        carriageCount = 1,
        rowsPerCarriage = 2,
        layoutColumns = Vector(
          TrainSeatLayoutColumn("A", 0, TrainSeatPositionType.Window),
          TrainSeatLayoutColumn("B", 1, TrainSeatPositionType.Aisle)
        ),
        seatIds = Vector("seat-1", "seat-2", "seat-3", "seat-4").map(TrainSeatId.apply)
      ).fold(throw _, identity)

    createTrainJourney(
      trainId = trainId,
      managerId = managerId,
      trainNumber = TrainNumber.unsafe("G100"),
      saleStartsAt = createdAt,
      stops = Vector(shaStop, nkgStop, hghStop),
      seatInventories = Vector(
        TrainSeatInventory(
          inventoryId = inventoryId,
          trainId = trainId,
          seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
          totalSeats = SeatCount.unsafe(4),
          saleableSeats = SeatCount.unsafe(4),
          seatInventoryStatus = TrainSeatInventoryStatus.OpenForSale
        )
      ),
      seats = seats,
      segmentPrices = Vector(
        TrainSegmentPrice(
          segmentPriceId = TrainSegmentPriceId("seg-1"),
          trainId = trainId,
          fromStopId = shaStop.stopId,
          toStopId = nkgStop.stopId,
          seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
          price = Money.unsafe(100, Currency.CNY)
        ),
        TrainSegmentPrice(
          segmentPriceId = TrainSegmentPriceId("seg-2"),
          trainId = trainId,
          fromStopId = nkgStop.stopId,
          toStopId = hghStop.stopId,
          seatClass = TrainSeatClass.unsafe("SECOND_CLASS"),
          price = Money.unsafe(80, Currency.CNY)
        )
      ),
      refundPolicySegments = Vector(
        TrainRefundPolicySegment(
          policySegmentId = TrainRefundPolicySegmentId("policy-1"),
          trainId = trainId,
          startOffsetBeforeDeparture = Duration.ofHours(24),
          endOffsetBeforeDeparture = Duration.ofHours(6),
          refundType = TrainRefundType.FullRefund,
          refundRate = RefundRate.unsafe(1)
        ),
        TrainRefundPolicySegment(
          policySegmentId = TrainRefundPolicySegmentId("policy-2"),
          trainId = trainId,
          startOffsetBeforeDeparture = Duration.ofHours(6),
          endOffsetBeforeDeparture = Duration.ofHours(1),
          refundType = TrainRefundType.PartialRefund,
          refundRate = RefundRate.unsafe(0.5)
        ),
        TrainRefundPolicySegment(
          policySegmentId = TrainRefundPolicySegmentId("policy-3"),
          trainId = trainId,
          startOffsetBeforeDeparture = Duration.ofHours(1),
          endOffsetBeforeDeparture = Duration.ZERO,
          refundType = TrainRefundType.NonRefundable,
          refundRate = RefundRate.unsafe(0)
        )
      ),
      createdAt = createdAt
    ).fold(throw _, identity)

