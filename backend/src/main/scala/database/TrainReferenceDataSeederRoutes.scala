// TrainReferenceDataSeederRoutes handles generated route templates.
package com.typesafe.travel.persistence

import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment

import java.time.{Instant, LocalDate, LocalTime, ZoneOffset}

object TrainReferenceDataSeederRoutes:
  import TrainReferenceDataSeederCore.*
  final case class RouteStationSeed(code: String, name: String)

  def routeStation(code: String): RouteStationSeed = RouteStationSeed(code, stationNameByCode(code))

  final case class RouteTemplateSeed(
      prefix: String,
      numberStart: Int,
      dailyDepartures: List[(Int, Int)],
      stations: List[RouteStationSeed],
      segmentMinutes: List[Int],
      baseSecondClassAmounts: List[BigDecimal]
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
        routeStation("BJS"),
        routeStation("TJS"),
        routeStation("JNW"),
        routeStation("NJS"),
        routeStation("SHH")
      ),
      segmentMinutes = List(28, 96, 180, 110),
      baseSecondClassAmounts = List(78, 102, 126, 150)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2101,
      dailyDepartures = List((6, 15), (9, 0), (12, 0), (15, 0), (18, 0)),
      stations = List(
        routeStation("BJX"),
        routeStation("ZZD"),
        routeStation("WUH"),
        routeStation("CSN"),
        routeStation("GZQ")
      ),
      segmentMinutes = List(84, 160, 142, 170),
      baseSecondClassAmounts = List(88, 114, 138, 162)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2201,
      dailyDepartures = List((6, 45), (9, 15), (12, 15), (15, 15), (18, 15)),
      stations = List(
        routeStation("SHN"),
        routeStation("HZD"),
        routeStation("NCX"),
        routeStation("CSN"),
        routeStation("SZN")
      ),
      segmentMinutes = List(42, 154, 146, 164),
      baseSecondClassAmounts = List(86, 112, 136, 158)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2301,
      dailyDepartures = List((7, 0), (9, 45), (12, 45), (15, 45), (18, 45)),
      stations = List(
        routeStation("CDD"),
        routeStation("CQB"),
        routeStation("WUH"),
        routeStation("CSN"),
        routeStation("XMN")
      ),
      segmentMinutes = List(68, 138, 132, 210),
      baseSecondClassAmounts = List(92, 118, 144, 172)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2401,
      dailyDepartures = List((7, 15), (10, 0), (13, 0), (16, 0), (19, 0)),
      stations = List(
        routeStation("XAB"),
        routeStation("ZZD"),
        routeStation("JNW"),
        routeStation("TJS"),
        routeStation("BJS")
      ),
      segmentMinutes = List(112, 126, 112, 104),
      baseSecondClassAmounts = List(94, 116, 142, 166)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2501,
      dailyDepartures = List((8, 0), (10, 45), (13, 45), (16, 45), (19, 45)),
      stations = List(
        routeStation("BJS"),
        routeStation("HFN"),
        routeStation("WUH"),
        routeStation("GZQ")
      ),
      segmentMinutes = List(98, 182, 228),
      baseSecondClassAmounts = List(136, 174, 212)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3001,
      dailyDepartures = List((8, 30), (11, 0), (14, 0), (17, 0), (20, 0)),
      stations = List(
        routeStation("SZB"),
        routeStation("NJS"),
        routeStation("HFN"),
        routeStation("WUH")
      ),
      segmentMinutes = List(52, 118, 182),
      baseSecondClassAmounts = List(62, 84, 110)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3101,
      dailyDepartures = List((6, 40), (9, 10), (12, 10), (15, 10), (18, 10)),
      stations = List(
        routeStation("QDB"),
        routeStation("TJS"),
        routeStation("BJS"),
        routeStation("TYN")
      ),
      segmentMinutes = List(100, 116, 198),
      baseSecondClassAmounts = List(68, 92, 118)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3201,
      dailyDepartures = List((7, 20), (10, 20), (13, 20), (16, 20), (19, 20)),
      stations = List(
        routeStation("NGB"),
        routeStation("WZS"),
        routeStation("FZN"),
        routeStation("XMN")
      ),
      segmentMinutes = List(66, 94, 178),
      baseSecondClassAmounts = List(60, 82, 108)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3301,
      dailyDepartures = List((8, 0), (11, 20), (14, 20), (17, 20), (20, 20)),
      stations = List(
        routeStation("XAB"),
        routeStation("ZZD"),
        routeStation("JNW"),
        routeStation("QDB")
      ),
      segmentMinutes = List(116, 128, 160),
      baseSecondClassAmounts = List(66, 88, 114)
    ),
    RouteTemplateSeed(
      prefix = "H",
      numberStart = 2601,
      dailyDepartures = List((7, 45), (10, 15), (13, 15), (16, 15), (19, 15)),
      stations = List(
        routeStation("SZN"),
        routeStation("GZQ"),
        routeStation("CSN"),
        routeStation("WUH"),
        routeStation("ZZD")
      ),
      segmentMinutes = List(34, 128, 146, 182),
      baseSecondClassAmounts = List(72, 98, 126, 154)
    ),
    RouteTemplateSeed(
      prefix = "B",
      numberStart = 3401,
      dailyDepartures = List((8, 15), (11, 15), (14, 15), (17, 15), (20, 15)),
      stations = List(
        routeStation("HZD"),
        routeStation("NCX"),
        routeStation("WUH"),
        routeStation("CDD")
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
    val trainNumberValue = template.numberStart + dayIndex * 1000 + templateIndex * departureCount + departureIndex
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

  val generatedTrainSeeds: List[TrainSeed] = buildGeneratedTrainSeeds()
