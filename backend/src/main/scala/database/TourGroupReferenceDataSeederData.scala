// TourGroupReferenceDataSeederData holds shared data models and SQL literal helpers.
package com.typesafe.travel.persistence

import doobie.*
import doobie.util.fragment.Fragment

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

object TourGroupReferenceDataSeederData:
  val DemoPassword = "12345qwert"
  val HzhDemoPassword = "hzhishengheng"
  val HzhDemoPasswordHash = "pbkdf2-sha256$65536$V5tXSQ3RRIvgkDcQzKKyNw==$QVeWVDg9AAXqIIAuQoUbzdVMycwonAXer22iZMl9fks="
  val DemoUserPrefix = "tour-user-"
  val DemoUserEmailDomain = "@demo.local"
  val DemoGroupPrefix = "tour-group-demo-"
  val SeededAt = Instant.parse("2026-05-18T00:00:00Z")
  val BaseStartDate = LocalDate.parse("2026-06-01")
  val StartDateSpanDays = 57L
  val ExpectedDemoGroupCount = 90L

  def sqlStringLiteral(value: String): Fragment =
    Fragment.const("'" + value.replace("'", "''") + "'")

  def sqlOptionalStringLiteral(value: Option[String]): Fragment =
    value match
      case Some(text) => sqlStringLiteral(text)
      case None       => Fragment.const("null")

  def sqlTimestampLiteral(value: Timestamp): Fragment =
    Fragment.const("TIMESTAMP '" + value.toLocalDateTime.toString.replace("'", "''") + "'")

  def sqlOptionalTimestampLiteral(value: Option[Timestamp]): Fragment =
    value match
      case Some(timestamp) => sqlTimestampLiteral(timestamp)
      case None            => Fragment.const("null")

  def sqlDateLiteral(value: Date): Fragment =
    Fragment.const("DATE '" + value.toString + "'")

  final case class DemoUserSeed(
      userId: String,
      email: String,
      phone: String,
      nickname: String,
      fullName: String,
      avatarUrl: String,
      birthDate: LocalDate,
      travelerType: String,
      documentNumber: String,
      documentExpiryDate: LocalDate,
      gender: String,
      points: Long
  )

  val HzhDemoUserSeed: DemoUserSeed =
    DemoUserSeed(
      userId = "user-9304a954-b91",
      email = "hzh@hh.com",
      phone = "00000000000",
      nickname = "hzh",
      fullName = "hzh",
      avatarUrl = "/uploads/avatars/user-9304a954-b91-20260408083354.jpg",
      birthDate = LocalDate.parse("2008-02-29"),
      travelerType = "AdultTraveler",
      documentNumber = "000000000000000000",
      documentExpiryDate = LocalDate.parse("2035-12-31"),
      gender = "男",
      points = 0L
    )

  final case class SeededDemoUser(
      userId: String,
      email: String,
      nickname: String,
      travelerId: String,
      birthDate: LocalDate
  )

  final case class CityResource(
      cityName: String,
      citySlug: String,
      primaryAirport: String,
      secondaryAirport: Option[String],
      hotelIds: Vector[String],
      attractionSuffixes: Vector[String]
  )

  final case class TrainRouteTemplate(
      trainId: String,
      fromCode: String,
      toCode: String,
      label: String
  )

  final case class JourneyTemplate(
      titlePrefix: String,
      descriptionPrefix: String,
      homeCity: CityResource,
      city1: CityResource,
      city2: CityResource,
      outboundDepartureAirport: String,
      outboundArrivalAirport: String,
      returnDepartureAirport: String,
      returnArrivalAirport: String,
      trainRoute: TrainRouteTemplate
  )

  final case class GroupSeed(
      groupIndex: Int,
      groupId: String,
      organizer: SeededDemoUser,
      members: Vector[SeededDemoUser],
      template: JourneyTemplate,
      startDate: LocalDate,
      endDate: LocalDate,
      capacity: Int,
      allowMemberDirectChat: Boolean,
      createdAt: Instant
  )

  final case class PlanOptionSeed(
      resourceType: String,
      resourceId: String,
      resourceVariantCode: Option[String],
      resourceContext: Option[String],
      label: String,
      description: String,
      defaultQuantity: Int
  )

  final case class PlanItemSeed(
      itemType: String,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int,
      options: Vector[PlanOptionSeed]
  )

  val cityResources = Vector(
    CityResource(
      cityName = "北京",
      citySlug = "beijing",
      primaryAirport = "PEK",
      secondaryAirport = Some("PKX"),
      hotelIds = Vector("hotel-beijing-guomen", "hotel-beijing-daxing"),
      attractionSuffixes = Vector("museum", "night-tour", "ancient", "science")
    ),
    CityResource(
      cityName = "上海",
      citySlug = "shanghai",
      primaryAirport = "SHA",
      secondaryAirport = Some("PVG"),
      hotelIds = Vector("hotel-sh-bund", "hotel-shanghai-hongqiao"),
      attractionSuffixes = Vector("museum", "garden", "theme-park", "night-tour")
    ),
    CityResource(
      cityName = "杭州",
      citySlug = "hangzhou",
      primaryAirport = "HGH",
      secondaryAirport = None,
      hotelIds = Vector("hotel-hz-westlake", "hotel-hangzhou-qiantang"),
      attractionSuffixes = Vector("garden", "museum", "night-tour", "science")
    ),
    CityResource(
      cityName = "广州",
      citySlug = "guangzhou",
      primaryAirport = "CAN",
      secondaryAirport = None,
      hotelIds = Vector("hotel-guangzhou-pearl", "hotel-guangzhou-baiyun"),
      attractionSuffixes = Vector("theme-park", "ocean-park", "museum", "night-tour")
    ),
    CityResource(
      cityName = "厦门",
      citySlug = "xiamen",
      primaryAirport = "XMN",
      secondaryAirport = None,
      hotelIds = Vector("hotel-xiamen-gulangyu", "hotel-xiamen-bay"),
      attractionSuffixes = Vector("art", "night-tour", "garden", "ancient")
    ),
    CityResource(
      cityName = "成都",
      citySlug = "chengdu",
      primaryAirport = "CTU",
      secondaryAirport = Some("TFU"),
      hotelIds = Vector("hotel-chengdu-tianfu", "hotel-chengdu-jinjiang"),
      attractionSuffixes = Vector("science", "museum", "theme-park", "ancient")
    ),
    CityResource(
      cityName = "长沙",
      citySlug = "changsha",
      primaryAirport = "CSX",
      secondaryAirport = None,
      hotelIds = Vector("hotel-changsha-yuelu", "hotel-changsha-xiangjiang"),
      attractionSuffixes = Vector("museum", "theme-park", "night-tour", "art")
    ),
    CityResource(
      cityName = "重庆",
      citySlug = "chongqing",
      primaryAirport = "CKG",
      secondaryAirport = None,
      hotelIds = Vector("hotel-chongqing-shancheng", "hotel-chongqing-jiangjing"),
      attractionSuffixes = Vector("night-tour", "art", "museum", "garden")
    ),
    CityResource(
      cityName = "西安",
      citySlug = "xian",
      primaryAirport = "XIY",
      secondaryAirport = None,
      hotelIds = Vector("hotel-xian-gudu", "hotel-xian-changan"),
      attractionSuffixes = Vector("ancient", "museum", "night-tour", "art")
    )
  )

  val trainRoutes = Vector(
    TrainRouteTemplate("train-hh306-h1001", "BJS", "SHH", "京沪高铁"),
    TrainRouteTemplate("train-hh306-h1002", "SHH", "WZS", "沪温动车"),
    TrainRouteTemplate("train-hh306-h1003", "GZQ", "XMN", "广厦城际"),
    TrainRouteTemplate("train-hh306-h1004", "CDD", "CSN", "成长高铁"),
    TrainRouteTemplate("train-hh306-h1005", "BJS", "GZQ", "京广高铁")
  )

  val journeyTemplates = Vector(
    JourneyTemplate(
      titlePrefix = "京沪杭慢游团",
      descriptionPrefix = "适合喜欢城市漫游与轻松节奏的游客",
      homeCity = cityResource("北京"),
      city1 = cityResource("上海"),
      city2 = cityResource("杭州"),
      outboundDepartureAirport = "PEK",
      outboundArrivalAirport = "SHA",
      returnDepartureAirport = "HGH",
      returnArrivalAirport = "PEK",
      trainRoute = trainRoutes(0)
    ),
    JourneyTemplate(
      titlePrefix = "沪穗厦海岛团",
      descriptionPrefix = "把城市美食、海岸风景和夜游行程一次串起来",
      homeCity = cityResource("上海"),
      city1 = cityResource("广州"),
      city2 = cityResource("厦门"),
      outboundDepartureAirport = "SHA",
      outboundArrivalAirport = "CAN",
      returnDepartureAirport = "XMN",
      returnArrivalAirport = "PVG",
      trainRoute = trainRoutes(2)
    ),
    JourneyTemplate(
      titlePrefix = "京蓉星城团",
      descriptionPrefix = "适合喜欢美食、火锅和轻松打卡的组合路线",
      homeCity = cityResource("北京"),
      city1 = cityResource("成都"),
      city2 = cityResource("长沙"),
      outboundDepartureAirport = "PEK",
      outboundArrivalAirport = "CTU",
      returnDepartureAirport = "CSX",
      returnArrivalAirport = "PEK",
      trainRoute = trainRoutes(3)
    ),
    JourneyTemplate(
      titlePrefix = "南方山城古都团",
      descriptionPrefix = "将山城夜景和古都文化放在同一条线上",
      homeCity = cityResource("广州"),
      city1 = cityResource("重庆"),
      city2 = cityResource("西安"),
      outboundDepartureAirport = "CAN",
      outboundArrivalAirport = "CKG",
      returnDepartureAirport = "XIY",
      returnArrivalAirport = "CAN",
      trainRoute = trainRoutes(4)
    )
  )

  def cityResource(cityName: String): CityResource =
    cityResources.find(_.cityName == cityName).getOrElse(throw new IllegalArgumentException(s"Unknown city resource: $cityName"))
