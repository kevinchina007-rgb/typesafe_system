// TrainReferenceDataSeederSupport 负责数据库基础设施相关实现。

package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.{CredentialStatus, hashPasswordForLoginEmail}
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment

import java.time.{Instant, LocalDate, LocalTime, ZoneOffset}

object TrainReferenceDataSeederSupport:
  final case class TrainStopSeed(
      stationCode: String,
      stationName: String,
      arrivalTime: Option[String],
      departureTime: Option[String]
  )

  final case class TrainSeatInventorySeed(
      seatClass: String,
      totalSeats: Int,
      saleableSeats: Int,
      carriageCount: Int,
      rowsPerCarriage: Int,
      seatLayoutSpec: String
  )

  final case class TrainSegmentPriceSeed(
      fromStationCode: String,
      toStationCode: String,
      seatClass: String,
      amount: BigDecimal
  )

  final case class TrainRefundPolicySeed(
      startOffsetMinutesBeforeDeparture: Long,
      endOffsetMinutesBeforeDeparture: Long,
      refundType: String,
      refundRate: BigDecimal
  )

  final case class TrainSeed(
      trainId: String,
      trainNumber: String,
      saleStartsAt: String,
      stops: List[TrainStopSeed],
      seatInventories: List[TrainSeatInventorySeed],
      segmentPrices: List[TrainSegmentPriceSeed],
      refundPolicies: List[TrainRefundPolicySeed]
  )

  final case class RouteStationSeed(code: String, name: String)

  final case class RouteTemplateSeed(
      prefix: String,
      numberStart: Int,
      dailyDepartures: List[(Int, Int)],
      stations: List[RouteStationSeed],
      segmentMinutes: List[Int],
      baseSecondClassAmounts: List[BigDecimal]
  )

  final case class SeatRowInsert(
      seatId: String,
      carriageNo: Int,
      rowNo: Int,
      seatCode: String,
      seatNo: String,
      seatLabel: String,
      seatPositionType: String
  )

  val defaultSeatInventories = List(
    TrainSeatInventorySeed("SECOND_CLASS", 60, 60, 2, 6, "A:Window,B:Middle,C:Aisle,D:Aisle,F:Window"),
    TrainSeatInventorySeed("FIRST_CLASS", 24, 24, 1, 6, "A:Window,C:Aisle,D:Aisle,F:Window"),
    TrainSeatInventorySeed("BUSINESS_CLASS", 8, 8, 1, 4, "A:Window,C:Aisle"),
    TrainSeatInventorySeed("SLEEPER", 12, 12, 1, 2, "A:Window,B:Middle,C:Other,D:Other,E:Middle,F:Window")
  )

  val defaultRefundPolicies = List(
    TrainRefundPolicySeed(1440, 720, "FullRefund", BigDecimal(1)),
    TrainRefundPolicySeed(720, 120, "PartialRefund", BigDecimal("0.8")),
    TrainRefundPolicySeed(120, 0, "NonRefundable", BigDecimal(0))
  )

  val baseDemoTrains = List(
    TrainSeed(
      trainId = "train-hh306-h1001",
      trainNumber = "H1001",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("BJS", "???", None, Some("2026-06-01T02:00:00Z")),
        TrainStopSeed("TJS", "???", Some("2026-06-01T02:28:00Z"), Some("2026-06-01T02:31:00Z")),
        TrainStopSeed("JNW", "???", Some("2026-06-01T04:10:00Z"), Some("2026-06-01T04:13:00Z")),
        TrainStopSeed("NJS", "???", Some("2026-06-01T07:10:00Z"), Some("2026-06-01T07:13:00Z")),
        TrainStopSeed("SHH", "????", Some("2026-06-01T09:05:00Z"), None)
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
      trainId = "train-hh306-h1002",
      trainNumber = "H1002",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("SHH", "????", None, Some("2026-06-01T01:00:00Z")),
        TrainStopSeed("SZB", "???", Some("2026-06-01T01:23:00Z"), Some("2026-06-01T01:26:00Z")),
        TrainStopSeed("HZD", "???", Some("2026-06-01T02:05:00Z"), Some("2026-06-01T02:08:00Z")),
        TrainStopSeed("NGB", "??", Some("2026-06-01T03:40:00Z"), Some("2026-06-01T03:43:00Z")),
        TrainStopSeed("WZS", "???", Some("2026-06-01T05:30:00Z"), None)
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
      trainId = "train-hh306-h1003",
      trainNumber = "H1003",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("GZQ", "???", None, Some("2026-06-01T02:00:00Z")),
        TrainStopSeed("SZN", "???", Some("2026-06-01T02:34:00Z"), Some("2026-06-01T02:37:00Z")),
        TrainStopSeed("XMN", "???", Some("2026-06-01T05:10:00Z"), None)
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
      trainId = "train-hh306-h1004",
      trainNumber = "H1004",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("CDD", "???", None, Some("2026-06-01T00:20:00Z")),
        TrainStopSeed("CQB", "???", Some("2026-06-01T01:34:00Z"), Some("2026-06-01T01:37:00Z")),
        TrainStopSeed("WUH", "??", Some("2026-06-01T04:20:00Z"), Some("2026-06-01T04:23:00Z")),
        TrainStopSeed("CSN", "???", Some("2026-06-01T06:25:00Z"), None)
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
      trainId = "train-hh306-h1005",
      trainNumber = "H1005",
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = List(
        TrainStopSeed("BJS", "???", None, Some("2026-06-01T03:00:00Z")),
        TrainStopSeed("ZZD", "???", Some("2026-06-01T05:35:00Z"), Some("2026-06-01T05:38:00Z")),
        TrainStopSeed("WUH", "??", Some("2026-06-01T07:55:00Z"), Some("2026-06-01T07:58:00Z")),
        TrainStopSeed("GZQ", "???", Some("2026-06-01T12:10:00Z"), None)
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

  val seatClassPriceMultipliers = List(
    "SECOND_CLASS" -> BigDecimal("1.00"),
    "FIRST_CLASS" -> BigDecimal("1.35"),
    "BUSINESS_CLASS" -> BigDecimal("1.90"),
    "SLEEPER" -> BigDecimal("1.60")
  )

  val generatedRouteTemplates = List(
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2001,
      dailyDepartures = List((6, 0), (8, 30), (11, 0), (14, 0), (17, 30)),
      stations = List(
        RouteStationSeed("BJS", "???"),
        RouteStationSeed("TJS", "???"),
        RouteStationSeed("JNW", "???"),
        RouteStationSeed("NJS", "???"),
        RouteStationSeed("SHH", "????")
      ),
      segmentMinutes = List(28, 96, 180, 110),
      baseSecondClassAmounts = List(78, 102, 126, 150)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2101,
      dailyDepartures = List((6, 15), (9, 0), (12, 0), (15, 0), (18, 0)),
      stations = List(
        RouteStationSeed("BJX", "???"),
        RouteStationSeed("ZZD", "???"),
        RouteStationSeed("WUH", "??"),
        RouteStationSeed("CSN", "???"),
        RouteStationSeed("GZQ", "???")
      ),
      segmentMinutes = List(84, 160, 142, 170),
      baseSecondClassAmounts = List(88, 114, 138, 162)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2201,
      dailyDepartures = List((6, 45), (9, 15), (12, 15), (15, 15), (18, 15)),
      stations = List(
        RouteStationSeed("SHN", "???"),
        RouteStationSeed("HZD", "???"),
        RouteStationSeed("NCX", "???"),
        RouteStationSeed("CSN", "???"),
        RouteStationSeed("SZN", "???")
      ),
      segmentMinutes = List(42, 154, 146, 164),
      baseSecondClassAmounts = List(86, 112, 136, 158)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2301,
      dailyDepartures = List((7, 0), (9, 45), (12, 45), (15, 45), (18, 45)),
      stations = List(
        RouteStationSeed("CDD", "???"),
        RouteStationSeed("CQB", "???"),
        RouteStationSeed("WUH", "??"),
        RouteStationSeed("CSN", "???"),
        RouteStationSeed("XMN", "???")
      ),
      segmentMinutes = List(68, 138, 132, 210),
      baseSecondClassAmounts = List(92, 118, 144, 172)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2401,
      dailyDepartures = List((7, 15), (10, 0), (13, 0), (16, 0), (19, 0)),
      stations = List(
        RouteStationSeed("XAB", "???"),
        RouteStationSeed("ZZD", "???"),
        RouteStationSeed("JNW", "???"),
        RouteStationSeed("TJS", "???"),
        RouteStationSeed("BJS", "???")
      ),
      segmentMinutes = List(112, 126, 112, 104),
      baseSecondClassAmounts = List(94, 116, 142, 166)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2501,
      dailyDepartures = List((8, 0), (10, 45), (13, 45), (16, 45), (19, 45)),
      stations = List(
        RouteStationSeed("BJS", "???"),
        RouteStationSeed("HFE", "???"),
        RouteStationSeed("WUH", "??"),
        RouteStationSeed("GZQ", "???")
      ),
      segmentMinutes = List(98, 182, 228),
      baseSecondClassAmounts = List(136, 174, 212)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3001,
      dailyDepartures = List((8, 30), (11, 0), (14, 0), (17, 0), (20, 0)),
      stations = List(
        RouteStationSeed("SZB", "???"),
        RouteStationSeed("NJS", "???"),
        RouteStationSeed("HFN", "???"),
        RouteStationSeed("WUH", "??")
      ),
      segmentMinutes = List(52, 118, 182),
      baseSecondClassAmounts = List(62, 84, 110)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3101,
      dailyDepartures = List((6, 40), (9, 10), (12, 10), (15, 10), (18, 10)),
      stations = List(
        RouteStationSeed("QDB", "???"),
        RouteStationSeed("TJS", "???"),
        RouteStationSeed("BJS", "???"),
        RouteStationSeed("TYN", "???")
      ),
      segmentMinutes = List(100, 116, 198),
      baseSecondClassAmounts = List(68, 92, 118)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3201,
      dailyDepartures = List((7, 20), (10, 20), (13, 20), (16, 20), (19, 20)),
      stations = List(
        RouteStationSeed("NGB", "??"),
        RouteStationSeed("WZS", "???"),
        RouteStationSeed("FZN", "???"),
        RouteStationSeed("XMN", "???")
      ),
      segmentMinutes = List(66, 94, 178),
      baseSecondClassAmounts = List(60, 82, 108)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3301,
      dailyDepartures = List((8, 0), (11, 20), (14, 20), (17, 20), (20, 20)),
      stations = List(
        RouteStationSeed("XAB", "???"),
        RouteStationSeed("ZZD", "???"),
        RouteStationSeed("JNW", "???"),
        RouteStationSeed("QDB", "???")
      ),
      segmentMinutes = List(116, 128, 160),
      baseSecondClassAmounts = List(66, 88, 114)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2601,
      dailyDepartures = List((7, 45), (10, 15), (13, 15), (16, 15), (19, 15)),
      stations = List(
        RouteStationSeed("SZN", "???"),
        RouteStationSeed("GZQ", "???"),
        RouteStationSeed("CSN", "???"),
        RouteStationSeed("WUH", "??"),
        RouteStationSeed("ZZD", "???")
      ),
      segmentMinutes = List(34, 128, 146, 182),
      baseSecondClassAmounts = List(72, 98, 126, 154)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3401,
      dailyDepartures = List((8, 15), (11, 15), (14, 15), (17, 15), (20, 15)),
      stations = List(
        RouteStationSeed("HZD", "???"),
        RouteStationSeed("NCX", "???"),
        RouteStationSeed("WUH", "??"),
        RouteStationSeed("CDD", "???")
      ),
      segmentMinutes = List(74, 182, 240),
      baseSecondClassAmounts = List(64, 96, 136)
    )
  )

  def buildGeneratedTrainSeeds(): List[TrainSeed] =
    val startDate = LocalDate.parse("2026-06-01")
    val endDate = LocalDate.parse("2026-07-31")
    val dates = Iterator.iterate(startDate)(_.plusDays(1)).takeWhile(date => !date.isAfter(endDate)).toList
    generatedRouteTemplates.zipWithIndex.flatMap { case (template, templateIndex) =>
      template.dailyDepartures.zipWithIndex.flatMap { case ((departureHour, departureMinute), departureIndex) =>
        dates.zipWithIndex.map { case (serviceDate, dayIndex) =>
          buildGeneratedTrainSeed(
            template,
            templateIndex,
            departureIndex,
            dayIndex,
            serviceDate,
            generatedRouteTemplates.size,
            template.dailyDepartures.size,
            departureHour,
            departureMinute
          )
        }
      }
    }

  def buildGeneratedTrainSeed(
      template: RouteTemplateSeed,
      templateIndex: Int,
      departureIndex: Int,
      dayIndex: Int,
      serviceDate: LocalDate,
      templateCount: Int,
      departureCount: Int,
      departureHour: Int,
      departureMinute: Int
  ): TrainSeed =
    val trainNumberValue = template.numberStart + dayIndex * templateCount * departureCount + templateIndex * departureCount + departureIndex
    val trainNumber = s"${template.prefix}$trainNumberValue"
    val trainId = s"train-hh306-${trainNumber.toLowerCase}"
    val departureInstant = localInstant(serviceDate, departureHour, departureMinute)
    val stops = buildRouteStops(template.stations, template.segmentMinutes, departureInstant)
    val segmentPrices = buildRouteSegmentPrices(stops, template.baseSecondClassAmounts)
    TrainSeed(
      trainId = trainId,
      trainNumber = trainNumber,
      saleStartsAt = "2026-05-18T00:00:00Z",
      stops = stops,
      seatInventories = defaultSeatInventories,
      segmentPrices = segmentPrices,
      refundPolicies = defaultRefundPolicies
    )

  def buildRouteStops(stations: List[RouteStationSeed], segmentMinutes: List[Int], departureInstant: Instant): List[TrainStopSeed] =
    if stations.size < 2 then throw new IllegalArgumentException("A generated train must have at least two stops")
    if segmentMinutes.size != stations.size - 1 then throw new IllegalArgumentException("Route segment minutes must match the number of adjacent station pairs")
    var current = departureInstant
    stations.zipWithIndex.map { case (station, index) =>
      if index == 0 then
        TrainStopSeed(station.code, station.name, None, Some(departureInstant.toString))
      else
        current = current.plusSeconds(segmentMinutes(index - 1).toLong * 60L)
        val arrival = current.toString
        if index == stations.size - 1 then
          TrainStopSeed(station.code, station.name, Some(arrival), None)
        else
          current = current.plusSeconds(3L * 60L)
          TrainStopSeed(station.code, station.name, Some(arrival), Some(current.toString))
    }

  def buildRouteSegmentPrices(stations: List[TrainStopSeed], baseSecondClassAmounts: List[BigDecimal]): List[TrainSegmentPriceSeed] =
    if stations.size < 2 then Nil
    else if baseSecondClassAmounts.size != stations.size - 1 then
      throw new IllegalArgumentException("Route segment prices must match the number of adjacent station pairs")
    else
      stations.zip(stations.drop(1)).zip(baseSecondClassAmounts).flatMap { case ((fromStop, toStop), baseSecondClassAmount) =>
        seatClassPriceMultipliers.map { case (seatClass, multiplier) =>
          TrainSegmentPriceSeed(
            fromStationCode = fromStop.stationCode,
            toStationCode = toStop.stationCode,
            seatClass = seatClass,
            amount = scaleTrainAmount(baseSecondClassAmount * multiplier)
          )
        }
      }

  def scaleTrainAmount(value: BigDecimal): BigDecimal =
    value.setScale(0, scala.math.BigDecimal.RoundingMode.HALF_UP)

  def localInstant(serviceDate: LocalDate, hour: Int, minute: Int): Instant =
    serviceDate.atTime(LocalTime.of(hour, minute)).atOffset(ZoneOffset.ofHours(8)).toInstant

  def timestamptzLiteral(value: Option[String]): Fragment =
    Fragment.const(
      value match
        case Some(raw) => s"cast('$raw' as timestamptz)"
        case None      => "null"
    )
  lazy val generatedTrainSeeds = buildGeneratedTrainSeeds()
  lazy val demoTrains = baseDemoTrains ++ generatedTrainSeeds
