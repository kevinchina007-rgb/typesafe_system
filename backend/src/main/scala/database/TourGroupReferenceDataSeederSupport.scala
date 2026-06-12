// TourGroupReferenceDataSeederSupport 璐熻矗鏁版嵁搴撳熀纭€璁炬柦鐩稿叧瀹炵幇銆?
package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment

import java.security.MessageDigest
import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate, ZoneOffset}
import java.util.HexFormat

object TourGroupReferenceDataSeederSupport:
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

  def cityResource(cityName: String): CityResource =
    cityResources.find(_.cityName == cityName).getOrElse(throw new IllegalArgumentException(s"Unknown city resource: $cityName"))

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
      cityName = "Beijing",
      citySlug = "beijing",
      primaryAirport = "PEK",
      secondaryAirport = Some("PKX"),
      hotelIds = Vector("hotel-beijing-guomen", "hotel-beijing-daxing"),
      attractionSuffixes = Vector("museum", "night-tour", "ancient", "science")
    ),
    CityResource(
      cityName = "Shanghai",
      citySlug = "shanghai",
      primaryAirport = "SHA",
      secondaryAirport = Some("PVG"),
      hotelIds = Vector("hotel-sh-bund", "hotel-shanghai-hongqiao"),
      attractionSuffixes = Vector("museum", "garden", "theme-park", "night-tour")
    ),
    CityResource(
      cityName = "Hangzhou",
      citySlug = "hangzhou",
      primaryAirport = "HGH",
      secondaryAirport = None,
      hotelIds = Vector("hotel-hz-westlake", "hotel-hangzhou-qiantang"),
      attractionSuffixes = Vector("garden", "museum", "night-tour", "science")
    ),
    CityResource(
      cityName = "Guangzhou",
      citySlug = "guangzhou",
      primaryAirport = "CAN",
      secondaryAirport = None,
      hotelIds = Vector("hotel-guangzhou-pearl", "hotel-guangzhou-baiyun"),
      attractionSuffixes = Vector("theme-park", "ocean-park", "museum", "night-tour")
    ),
    CityResource(
      cityName = "Xiamen",
      citySlug = "xiamen",
      primaryAirport = "XMN",
      secondaryAirport = None,
      hotelIds = Vector("hotel-xiamen-gulangyu", "hotel-xiamen-bay"),
      attractionSuffixes = Vector("art", "night-tour", "garden", "ancient")
    ),
    CityResource(
      cityName = "Chengdu",
      citySlug = "chengdu",
      primaryAirport = "CTU",
      secondaryAirport = Some("TFU"),
      hotelIds = Vector("hotel-chengdu-tianfu", "hotel-chengdu-jinjiang"),
      attractionSuffixes = Vector("science", "museum", "theme-park", "ancient")
    ),
    CityResource(
      cityName = "Changsha",
      citySlug = "changsha",
      primaryAirport = "CSX",
      secondaryAirport = None,
      hotelIds = Vector("hotel-changsha-yuelu", "hotel-changsha-xiangjiang"),
      attractionSuffixes = Vector("museum", "theme-park", "night-tour", "art")
    ),
    CityResource(
      cityName = "Chongqing",
      citySlug = "chongqing",
      primaryAirport = "CKG",
      secondaryAirport = None,
      hotelIds = Vector("hotel-chongqing-shancheng", "hotel-chongqing-jiangjing"),
      attractionSuffixes = Vector("night-tour", "art", "museum", "garden")
    ),
    CityResource(
      cityName = "Xian",
      citySlug = "xian",
      primaryAirport = "XIY",
      secondaryAirport = None,
      hotelIds = Vector("hotel-xian-gudu", "hotel-xian-changan"),
      attractionSuffixes = Vector("ancient", "museum", "night-tour", "art")
    )
  )

  val trainRoutes = Vector(
    TrainRouteTemplate("train-hh306-h1001", "BJS", "SHH", "Route 1"),
    TrainRouteTemplate("train-hh306-h1002", "SHH", "WZS", "Route 2"),
    TrainRouteTemplate("train-hh306-h1003", "GZQ", "XMN", "Route 3"),
    TrainRouteTemplate("train-hh306-h1004", "CDD", "CSN", "Route 4"),
    TrainRouteTemplate("train-hh306-h1005", "BJS", "GZQ", "Route 5")
  )

  val journeyTemplates = Vector(
    JourneyTemplate(
      titlePrefix = "Journey Template",
      descriptionPrefix = "Tour group demo route",
      homeCity = cityResource("Beijing"),
      city1 = cityResource("Shanghai"),
      city2 = cityResource("Hangzhou"),
      outboundDepartureAirport = "PEK",
      outboundArrivalAirport = "SHA",
      returnDepartureAirport = "HGH",
      returnArrivalAirport = "PEK",
      trainRoute = trainRoutes(0)
    ),
    JourneyTemplate(
      titlePrefix = "Journey Template",
      descriptionPrefix = "Tour group demo route",
      homeCity = cityResource("Beijing"),
      city1 = cityResource("Shanghai"),
      city2 = cityResource("Hangzhou"),
      outboundDepartureAirport = "SHA",
      outboundArrivalAirport = "CAN",
      returnDepartureAirport = "XMN",
      returnArrivalAirport = "PVG",
      trainRoute = trainRoutes(2)
    ),
    JourneyTemplate(
      titlePrefix = "Journey Template",
      descriptionPrefix = "Tour group demo route",
      homeCity = cityResource("Beijing"),
      city1 = cityResource("Shanghai"),
      city2 = cityResource("Hangzhou"),
      outboundDepartureAirport = "PEK",
      outboundArrivalAirport = "CTU",
      returnDepartureAirport = "CSX",
      returnArrivalAirport = "PEK",
      trainRoute = trainRoutes(3)
    ),
    JourneyTemplate(
      titlePrefix = "Journey Template",
      descriptionPrefix = "Tour group demo route",
      homeCity = cityResource("Beijing"),
      city1 = cityResource("Shanghai"),
      city2 = cityResource("Hangzhou"),
      outboundDepartureAirport = "CAN",
      outboundArrivalAirport = "CKG",
      returnDepartureAirport = "XIY",
      returnArrivalAirport = "CAN",
      trainRoute = trainRoutes(4)
    )
  )
