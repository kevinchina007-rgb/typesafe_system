package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.{CredentialStatus, hashPasswordForLoginEmail}
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment

import java.time.Instant

object TrainReferenceDataSeeder:
  private val DemoManagerId = "manager-train-hh306"
  private val DemoManagerEmail = "ops@hh306.example"
  private val DemoManagerDisplayName = "哼哼306"
  private val DemoManagerOperatorCode = "HH306"
  private val DemoManagerPassword = "Train2026!"
  private val SeededAt = Instant.parse("2026-05-18T00:00:00Z")

  private final case class TrainStopSeed(
      stationCode: String,
      stationName: String,
      arrivalTime: Option[String],
      departureTime: Option[String]
  )

  private final case class TrainSeatInventorySeed(
      seatClass: String,
      totalSeats: Int,
      saleableSeats: Int,
      carriageCount: Int,
      rowsPerCarriage: Int,
      seatLayoutSpec: String
  )

  private final case class TrainSegmentPriceSeed(
      fromStationCode: String,
      toStationCode: String,
      seatClass: String,
      amount: BigDecimal
  )

  private final case class TrainRefundPolicySeed(
      startOffsetMinutesBeforeDeparture: Long,
      endOffsetMinutesBeforeDeparture: Long,
      refundType: String,
      refundRate: BigDecimal
  )

  private final case class TrainSeed(
      trainId: String,
      trainNumber: String,
      saleStartsAt: String,
      stops: List[TrainStopSeed],
      seatInventories: List[TrainSeatInventorySeed],
      segmentPrices: List[TrainSegmentPriceSeed],
      refundPolicies: List[TrainRefundPolicySeed]
  )

  private final case class SeatRowInsert(
      seatId: String,
      carriageNo: Int,
      rowNo: Int,
      seatCode: String,
      seatNo: String,
      seatLabel: String,
      seatPositionType: String
  )

  private val defaultSeatInventories = List(
    TrainSeatInventorySeed("SECOND_CLASS", 60, 60, 2, 6, "A:Window,B:Middle,C:Aisle,D:Aisle,F:Window"),
    TrainSeatInventorySeed("FIRST_CLASS", 24, 24, 1, 6, "A:Window,C:Aisle,D:Aisle,F:Window"),
    TrainSeatInventorySeed("BUSINESS_CLASS", 8, 8, 1, 4, "A:Window,C:Aisle"),
    TrainSeatInventorySeed("SLEEPER", 12, 12, 1, 2, "A:Window,B:Middle,C:Other,D:Other,E:Middle,F:Window")
  )

  private val defaultRefundPolicies = List(
    TrainRefundPolicySeed(1440, 720, "FullRefund", BigDecimal(1)),
    TrainRefundPolicySeed(720, 120, "PartialRefund", BigDecimal("0.8")),
    TrainRefundPolicySeed(120, 0, "NonRefundable", BigDecimal(0))
  )

  private val demoTrains = List(
    TrainSeed(
      trainId = "train-hh306-g1001",
      trainNumber = "G1001",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("BJS", "北京南", None, Some("2026-06-01T02:00:00Z")),
        TrainStopSeed("TJS", "天津南", Some("2026-06-01T02:28:00Z"), Some("2026-06-01T02:31:00Z")),
        TrainStopSeed("JNW", "济南西", Some("2026-06-01T04:10:00Z"), Some("2026-06-01T04:13:00Z")),
        TrainStopSeed("NJS", "南京南", Some("2026-06-01T07:10:00Z"), Some("2026-06-01T07:13:00Z")),
        TrainStopSeed("SHH", "上海虹桥", Some("2026-06-01T09:05:00Z"), None)
      ),
      seatInventories = defaultSeatInventories,
      segmentPrices = List(
        TrainSegmentPriceSeed("BJS", "TJS", "SECOND_CLASS", 78),
        TrainSegmentPriceSeed("TJS", "JNW", "SECOND_CLASS", 122),
        TrainSegmentPriceSeed("JNW", "NJS", "SECOND_CLASS", 146),
        TrainSegmentPriceSeed("NJS", "SHH", "SECOND_CLASS", 164),
        TrainSegmentPriceSeed("BJS", "TJS", "FIRST_CLASS", 118),
        TrainSegmentPriceSeed("TJS", "JNW", "FIRST_CLASS", 168),
        TrainSegmentPriceSeed("JNW", "NJS", "FIRST_CLASS", 196),
        TrainSegmentPriceSeed("NJS", "SHH", "FIRST_CLASS", 218),
        TrainSegmentPriceSeed("BJS", "TJS", "BUSINESS_CLASS", 198),
        TrainSegmentPriceSeed("TJS", "JNW", "BUSINESS_CLASS", 248),
        TrainSegmentPriceSeed("JNW", "NJS", "BUSINESS_CLASS", 292),
        TrainSegmentPriceSeed("NJS", "SHH", "BUSINESS_CLASS", 336),
        TrainSegmentPriceSeed("BJS", "TJS", "SLEEPER", 168),
        TrainSegmentPriceSeed("TJS", "JNW", "SLEEPER", 210),
        TrainSegmentPriceSeed("JNW", "NJS", "SLEEPER", 254),
        TrainSegmentPriceSeed("NJS", "SHH", "SLEEPER", 298)
      ),
      refundPolicies = defaultRefundPolicies
    ),
    TrainSeed(
      trainId = "train-hh306-g1002",
      trainNumber = "G1002",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("SHH", "上海虹桥", None, Some("2026-06-01T01:00:00Z")),
        TrainStopSeed("SZB", "苏州北", Some("2026-06-01T01:23:00Z"), Some("2026-06-01T01:26:00Z")),
        TrainStopSeed("HZD", "杭州东", Some("2026-06-01T02:05:00Z"), Some("2026-06-01T02:08:00Z")),
        TrainStopSeed("NGB", "宁波", Some("2026-06-01T03:40:00Z"), Some("2026-06-01T03:43:00Z")),
        TrainStopSeed("WZS", "温州南", Some("2026-06-01T05:30:00Z"), None)
      ),
      seatInventories = defaultSeatInventories,
      segmentPrices = List(
        TrainSegmentPriceSeed("SHH", "SZB", "SECOND_CLASS", 48),
        TrainSegmentPriceSeed("SZB", "HZD", "SECOND_CLASS", 66),
        TrainSegmentPriceSeed("HZD", "NGB", "SECOND_CLASS", 94),
        TrainSegmentPriceSeed("NGB", "WZS", "SECOND_CLASS", 112),
        TrainSegmentPriceSeed("SHH", "SZB", "FIRST_CLASS", 88),
        TrainSegmentPriceSeed("SZB", "HZD", "FIRST_CLASS", 108),
        TrainSegmentPriceSeed("HZD", "NGB", "FIRST_CLASS", 136),
        TrainSegmentPriceSeed("NGB", "WZS", "FIRST_CLASS", 158),
        TrainSegmentPriceSeed("SHH", "SZB", "BUSINESS_CLASS", 148),
        TrainSegmentPriceSeed("SZB", "HZD", "BUSINESS_CLASS", 188),
        TrainSegmentPriceSeed("HZD", "NGB", "BUSINESS_CLASS", 228),
        TrainSegmentPriceSeed("NGB", "WZS", "BUSINESS_CLASS", 268),
        TrainSegmentPriceSeed("SHH", "SZB", "SLEEPER", 138),
        TrainSegmentPriceSeed("SZB", "HZD", "SLEEPER", 166),
        TrainSegmentPriceSeed("HZD", "NGB", "SLEEPER", 198),
        TrainSegmentPriceSeed("NGB", "WZS", "SLEEPER", 226)
      ),
      refundPolicies = defaultRefundPolicies
    ),
    TrainSeed(
      trainId = "train-hh306-g1003",
      trainNumber = "G1003",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("GZQ", "广州南", None, Some("2026-06-01T02:00:00Z")),
        TrainStopSeed("SZN", "深圳北", Some("2026-06-01T02:34:00Z"), Some("2026-06-01T02:37:00Z")),
        TrainStopSeed("XMN", "厦门北", Some("2026-06-01T05:10:00Z"), None)
      ),
      seatInventories = defaultSeatInventories,
      segmentPrices = List(
        TrainSegmentPriceSeed("GZQ", "SZN", "SECOND_CLASS", 128),
        TrainSegmentPriceSeed("SZN", "XMN", "SECOND_CLASS", 176),
        TrainSegmentPriceSeed("GZQ", "SZN", "FIRST_CLASS", 166),
        TrainSegmentPriceSeed("SZN", "XMN", "FIRST_CLASS", 228),
        TrainSegmentPriceSeed("GZQ", "SZN", "BUSINESS_CLASS", 246),
        TrainSegmentPriceSeed("SZN", "XMN", "BUSINESS_CLASS", 338),
        TrainSegmentPriceSeed("GZQ", "SZN", "SLEEPER", 212),
        TrainSegmentPriceSeed("SZN", "XMN", "SLEEPER", 286)
      ),
      refundPolicies = defaultRefundPolicies
    ),
    TrainSeed(
      trainId = "train-hh306-g1004",
      trainNumber = "G1004",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("CDD", "成都东", None, Some("2026-06-01T00:20:00Z")),
        TrainStopSeed("CQB", "重庆北", Some("2026-06-01T01:34:00Z"), Some("2026-06-01T01:37:00Z")),
        TrainStopSeed("WUH", "武汉", Some("2026-06-01T04:20:00Z"), Some("2026-06-01T04:23:00Z")),
        TrainStopSeed("CSN", "长沙南", Some("2026-06-01T06:25:00Z"), None)
      ),
      seatInventories = defaultSeatInventories,
      segmentPrices = List(
        TrainSegmentPriceSeed("CDD", "CQB", "SECOND_CLASS", 112),
        TrainSegmentPriceSeed("CQB", "WUH", "SECOND_CLASS", 138),
        TrainSegmentPriceSeed("WUH", "CSN", "SECOND_CLASS", 182),
        TrainSegmentPriceSeed("CDD", "CQB", "FIRST_CLASS", 152),
        TrainSegmentPriceSeed("CQB", "WUH", "FIRST_CLASS", 192),
        TrainSegmentPriceSeed("WUH", "CSN", "FIRST_CLASS", 238),
        TrainSegmentPriceSeed("CDD", "CQB", "BUSINESS_CLASS", 214),
        TrainSegmentPriceSeed("CQB", "WUH", "BUSINESS_CLASS", 276),
        TrainSegmentPriceSeed("WUH", "CSN", "BUSINESS_CLASS", 330),
        TrainSegmentPriceSeed("CDD", "CQB", "SLEEPER", 186),
        TrainSegmentPriceSeed("CQB", "WUH", "SLEEPER", 228),
        TrainSegmentPriceSeed("WUH", "CSN", "SLEEPER", 286)
      ),
      refundPolicies = defaultRefundPolicies
    ),
    TrainSeed(
      trainId = "train-hh306-g1005",
      trainNumber = "G1005",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("BJS", "北京南", None, Some("2026-06-01T03:00:00Z")),
        TrainStopSeed("ZZD", "郑州东", Some("2026-06-01T05:35:00Z"), Some("2026-06-01T05:38:00Z")),
        TrainStopSeed("WUH", "武汉", Some("2026-06-01T07:55:00Z"), Some("2026-06-01T07:58:00Z")),
        TrainStopSeed("GZQ", "广州南", Some("2026-06-01T12:10:00Z"), None)
      ),
      seatInventories = defaultSeatInventories,
      segmentPrices = List(
        TrainSegmentPriceSeed("BJS", "ZZD", "SECOND_CLASS", 158),
        TrainSegmentPriceSeed("ZZD", "WUH", "SECOND_CLASS", 188),
        TrainSegmentPriceSeed("WUH", "GZQ", "SECOND_CLASS", 228),
        TrainSegmentPriceSeed("BJS", "ZZD", "FIRST_CLASS", 198),
        TrainSegmentPriceSeed("ZZD", "WUH", "FIRST_CLASS", 228),
        TrainSegmentPriceSeed("WUH", "GZQ", "FIRST_CLASS", 278),
        TrainSegmentPriceSeed("BJS", "ZZD", "BUSINESS_CLASS", 268),
        TrainSegmentPriceSeed("ZZD", "WUH", "BUSINESS_CLASS", 318),
        TrainSegmentPriceSeed("WUH", "GZQ", "BUSINESS_CLASS", 388),
        TrainSegmentPriceSeed("BJS", "ZZD", "SLEEPER", 228),
        TrainSegmentPriceSeed("ZZD", "WUH", "SLEEPER", 268),
        TrainSegmentPriceSeed("WUH", "GZQ", "SLEEPER", 328)
      ),
      refundPolicies = defaultRefundPolicies
    )
  )

  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      _ <- seedManagerIfNeeded(transactor)
      _ <- demoTrains.traverse_(seedTrainIfMissing(transactor, _))
    yield ()

  private def seedManagerIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      count <- sql"""
        select count(*)
        from railway_managers
        where manager_id = $DemoManagerId or email = $DemoManagerEmail
      """.query[Long].unique.transact(transactor)
      _ <- if count > 0 then IO.unit else insertManager(transactor)
    yield ()

  private def insertManager(transactor: Transactor[IO]): IO[Unit] =
    for
      emailAddress <- IO.fromEither(EmailAddress.create(DemoManagerEmail))
      passwordHash <- hashPasswordForLoginEmail(DemoManagerPassword, emailAddress)
      _ <- insertManagerRows(passwordHash).transact(transactor)
    yield ()

  private def insertManagerRows(passwordHash: String): ConnectionIO[Unit] =
    val seededAtValue = SeededAt.toString
    for
      _ <- sql"""
        insert into railway_managers (manager_id, operator_code, email, display_name, status, created_at)
        values ($DemoManagerId, $DemoManagerOperatorCode, $DemoManagerEmail, $DemoManagerDisplayName, ${"Active"}, cast($seededAtValue as timestamptz))
        on conflict (manager_id) do update set
          operator_code = excluded.operator_code,
          email = excluded.email,
          display_name = excluded.display_name,
          status = excluded.status,
          created_at = excluded.created_at
      """.update.run.void
      _ <- sql"""
        insert into manager_credentials (
          credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at
        ) values (
          ${s"credential-train-$DemoManagerId"},
          ${"Train"},
          $DemoManagerId,
          $DemoManagerEmail,
          $passwordHash,
          ${CredentialStatus.Active.toString},
          cast($seededAtValue as timestamptz),
          cast($seededAtValue as timestamptz),
          cast($seededAtValue as timestamptz)
        )
        on conflict (manager_type, manager_id) do update set
          login_email = excluded.login_email,
          password_hash = excluded.password_hash,
          status = excluded.status,
          updated_at = excluded.updated_at,
          password_updated_at = excluded.password_updated_at
      """.update.run.void
    yield ()

  private def seedTrainIfMissing(transactor: Transactor[IO], seed: TrainSeed): IO[Unit] =
    for
      count <- sql"""select count(*) from trains where train_id = ${seed.trainId}""".query[Long].unique.transact(transactor)
      _ <- if count > 0 then IO.unit else insertTrain(transactor, seed)
    yield ()

  private def insertTrain(transactor: Transactor[IO], seed: TrainSeed): IO[Unit] =
    insertTrainRows(seed).transact(transactor).void

  private def insertTrainRows(seed: TrainSeed): ConnectionIO[Unit] =
    val stopIds = seed.stops.zipWithIndex.map { case (stop, index) => stop.stationCode -> stopId(seed.trainId, index + 1) }.toMap
    val saleStartsAtValue = Instant.parse(seed.saleStartsAt).toString
    val seededAtValue = SeededAt.toString
    for
      _ <- sql"""
        insert into trains (train_id, manager_id, train_number, sale_starts_at, status, created_at)
        values (${seed.trainId}, $DemoManagerId, ${seed.trainNumber}, cast($saleStartsAtValue as timestamptz), ${"OnSale"}, cast($seededAtValue as timestamptz))
      """.update.run.void
      _ <- insertStops(seed).void
      _ <- insertSeatInventories(seed).void
      _ <- insertSeats(seed).void
      _ <- insertSegmentPrices(seed, stopIds).void
      _ <- insertRefundPolicies(seed).void
    yield ()

  private def insertStops(seed: TrainSeed): ConnectionIO[Unit] =
    seed.stops.zipWithIndex.traverse_ { case (stop, index) =>
      val arrivalTimeValue = timestamptzLiteral(stop.arrivalTime)
      val departureTimeValue = timestamptzLiteral(stop.departureTime)
      sql"""
        insert into train_stops (stop_id, train_id, station_code, station_name, sequence_no, arrival_time, departure_time)
        values (
          ${stopId(seed.trainId, index + 1)},
          ${seed.trainId},
          ${stop.stationCode},
          ${stop.stationName},
          ${index + 1},
          $arrivalTimeValue,
          $departureTimeValue
        )
      """.update.run.void
    }

  private def insertSeatInventories(seed: TrainSeed): ConnectionIO[Unit] =
    seed.seatInventories.traverse_ { inventory =>
      sql"""
        insert into train_seat_inventories (inventory_id, train_id, seat_class, total_seats, saleable_seats, status)
        values (
          ${inventoryId(seed.trainId, inventory.seatClass)},
          ${seed.trainId},
          ${inventory.seatClass},
          ${inventory.totalSeats},
          ${inventory.saleableSeats},
          ${"OpenForSale"}
        )
      """.update.run.void
    }

  private def insertSeats(seed: TrainSeed): ConnectionIO[Unit] =
    seed.seatInventories.traverse_ { inventory =>
      val seatRows = generateSeatRows(seed.trainId, inventory)
      seatRows.traverse_ { seatRow =>
        sql"""
          insert into train_seats (
            seat_id, train_id, inventory_id, seat_class, carriage_no, row_no, seat_code, seat_no, seat_label, seat_position_type, status
          ) values (
            ${seatRow.seatId},
            ${seed.trainId},
            ${inventoryId(seed.trainId, inventory.seatClass)},
            ${inventory.seatClass},
            ${seatRow.carriageNo},
            ${seatRow.rowNo},
            ${seatRow.seatCode},
            ${seatRow.seatNo},
            ${seatRow.seatLabel},
            ${seatRow.seatPositionType},
            ${"Available"}
          )
        """.update.run.void
      }
    }

  private def insertSegmentPrices(seed: TrainSeed, stopIds: Map[String, String]): ConnectionIO[Unit] =
    seed.segmentPrices.traverse_ { price =>
      sql"""
        insert into train_segment_prices (segment_price_id, train_id, from_stop_id, to_stop_id, seat_class, amount, currency)
        values (
          ${segmentPriceId(seed.trainId, price)},
          ${seed.trainId},
          ${stopIds.getOrElse(price.fromStationCode, throw new IllegalArgumentException(s"Train '${seed.trainId}' is missing station '${price.fromStationCode}'"))},
          ${stopIds.getOrElse(price.toStationCode, throw new IllegalArgumentException(s"Train '${seed.trainId}' is missing station '${price.toStationCode}'"))},
          ${price.seatClass},
          ${price.amount},
          ${"CNY"}
        )
      """.update.run.void
    }

  private def insertRefundPolicies(seed: TrainSeed): ConnectionIO[Unit] =
    seed.refundPolicies.traverse_ { policy =>
      sql"""
        insert into train_refund_policy_segments (
          policy_segment_id, train_id, start_offset_minutes_before_departure, end_offset_minutes_before_departure, refund_type, refund_rate
        ) values (
          ${refundPolicyId(seed.trainId, policy)},
          ${seed.trainId},
          ${policy.startOffsetMinutesBeforeDeparture},
          ${policy.endOffsetMinutesBeforeDeparture},
          ${policy.refundType},
          ${policy.refundRate}
        )
      """.update.run.void
    }

  private def generateSeatRows(trainId: String, inventory: TrainSeatInventorySeed): Vector[SeatRowInsert] =
    val layoutColumns = parseSeatLayoutSpec(inventory.seatLayoutSpec)
    val expectedSeatCount = inventory.carriageCount * inventory.rowsPerCarriage * layoutColumns.size
    if expectedSeatCount != inventory.totalSeats then
      throw new IllegalArgumentException(
        s"Seat inventory '${inventory.seatClass}' on '$trainId' expected $expectedSeatCount seats but declares ${inventory.totalSeats}"
      )

    (1 to inventory.carriageCount).toVector.flatMap { carriageNo =>
      (1 to inventory.rowsPerCarriage).toVector.flatMap { rowNo =>
        layoutColumns.map { case (seatCode, seatPositionType) =>
          val seatNo = f"$rowNo%02d$seatCode"
          SeatRowInsert(
            seatId = s"${inventoryId(trainId, inventory.seatClass)}-$carriageNo-$rowNo-$seatCode",
            carriageNo = carriageNo,
            rowNo = rowNo,
            seatCode = seatCode,
            seatNo = seatNo,
            seatLabel = s"$carriageNo-$seatNo",
            seatPositionType = seatPositionType
          )
        }
      }
    }

  private def parseSeatLayoutSpec(layoutSpec: String): Vector[(String, String)] =
    layoutSpec
      .split(',')
      .toVector
      .map(_.trim)
      .filter(_.nonEmpty)
      .map { item =>
        val parts = item.split(':').map(_.trim)
        if parts.length < 2 then throw new IllegalArgumentException(s"Seat layout spec '$layoutSpec' is invalid")
        (parts(0), com.typesafe.travel.train.domain.TrainSeatPositionType.fromText(parts(1)).toString)
      }

  private def inventoryId(trainId: String, seatClass: String): String =
    s"$trainId-${seatClass.toLowerCase.replace('_', '-')}"

  private def stopId(trainId: String, sequenceNo: Int): String =
    s"$trainId-stop-$sequenceNo"

  private def segmentPriceId(trainId: String, price: TrainSegmentPriceSeed): String =
    s"$trainId-${price.seatClass.toLowerCase}-${price.fromStationCode.toLowerCase}-${price.toStationCode.toLowerCase}"

  private def refundPolicyId(trainId: String, policy: TrainRefundPolicySeed): String =
    s"$trainId-${policy.startOffsetMinutesBeforeDeparture}-${policy.endOffsetMinutesBeforeDeparture}"

  private def timestamptzLiteral(value: Option[String]): Fragment =
    Fragment.const(
      value match
        case Some(raw) => s"cast('$raw' as timestamptz)"
        case None      => "null"
    )
