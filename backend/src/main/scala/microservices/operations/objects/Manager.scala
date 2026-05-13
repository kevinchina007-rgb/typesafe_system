package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*
import java.time.Instant

final case class ManagerStatus(value: String):
  override def toString: String = value

object ManagerStatus:
  val Active: ManagerStatus = ManagerStatus("Active")
  val Inactive: ManagerStatus = ManagerStatus("Inactive")
  given sourceEncoder: Encoder[ManagerStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ManagerStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ManagerStatus =
    value.trim.toLowerCase match
      case "inactive" => Inactive
      case _ => Active

final case class ManagerType(value: String):
  override def toString: String = value

object ManagerType:
  val Airline: ManagerType = ManagerType("Airline")
  val Hotel: ManagerType = ManagerType("Hotel")
  val Attraction: ManagerType = ManagerType("Attraction")
  given sourceEncoder: Encoder[ManagerType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ManagerType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ManagerType =
    value.trim.toLowerCase match
      case "hotel" => Hotel
      case "attraction" => Attraction
      case _ => Airline

sealed trait ManagerContext

final case class AirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Airline
object AirlineManager:
  import ManagerSourceJsonCodecs.given
  given sourceEncoder: Encoder[AirlineManager] = deriveEncoder
  given sourceDecoder: Decoder[AirlineManager] = deriveDecoder

final case class HotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Hotel
object HotelManager:
  import ManagerSourceJsonCodecs.given
  given sourceEncoder: Encoder[HotelManager] = deriveEncoder
  given sourceDecoder: Decoder[HotelManager] = deriveDecoder

final case class AttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
) extends ManagerContext:
  val managerType: ManagerType = ManagerType.Attraction
object AttractionManager:
  import ManagerSourceJsonCodecs.given
  given sourceEncoder: Encoder[AttractionManager] = deriveEncoder
  given sourceDecoder: Decoder[AttractionManager] = deriveDecoder

object ManagerContext:
  given sourceEncoder: Encoder[ManagerContext] =
    Encoder.instance {
      case manager: AirlineManager =>
        manager.asJson.deepMerge(Json.obj("managerContextType" -> Json.fromString("Airline")))
      case manager: HotelManager =>
        manager.asJson.deepMerge(Json.obj("managerContextType" -> Json.fromString("Hotel")))
      case manager: AttractionManager =>
        manager.asJson.deepMerge(Json.obj("managerContextType" -> Json.fromString("Attraction")))
    }
  given sourceDecoder: Decoder[ManagerContext] =
    Decoder.instance { cursor =>
      cursor.downField("managerContextType").as[String].flatMap {
        case "Airline"    => cursor.as[AirlineManager]
        case "Hotel"      => cursor.as[HotelManager]
        case "Attraction" => cursor.as[AttractionManager]
        case other        => Left(io.circe.DecodingFailure(s"Unknown manager context type: $other", cursor.history))
      }
    }

sealed trait ManagerError extends DomainError:
  def message: String

object ManagerError:
  final case class ManagerWasNotFoundByEmail(managerType: ManagerType, primaryEmailAddress: EmailAddress) extends ManagerError:
    override val message: String = s"$managerType manager '${primaryEmailAddress.value}' was not found"

  final case class ManagerWasNotFoundById(managerType: ManagerType, managerId: ManagerId) extends ManagerError:
    override val message: String = s"$managerType manager '${managerId.value}' was not found"

  final case class ManagerEmailAlreadyExists(primaryEmailAddress: EmailAddress) extends ManagerError:
    override val message: String = s"Manager email '${primaryEmailAddress.value}' already exists"

  final case class ManagerWasInactive(managerType: ManagerType, managerId: ManagerId) extends ManagerError:
    override val message: String = s"$managerType manager '${managerId.value}' is inactive"

  final case class ManagerScopeDidNotMatch(managerType: ManagerType, managerId: ManagerId, orderItemId: OrderItemId) extends ManagerError:
    override val message: String = s"$managerType manager '${managerId.value}' cannot act on booking item '${orderItemId.value}'"
