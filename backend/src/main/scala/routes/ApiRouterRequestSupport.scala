package com.typesafe.travel.api

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.operations.domain.ManagerType
import com.typesafe.travel.order.domain.{Order, SupplierReviewStatus}
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerEmergencyContact
import com.typesafe.travel.inventory.domain.InventoryReservationRepository
import io.circe.syntax.*
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.multipart.Multipart

import java.time.LocalDate

trait ApiRouterRequestSupport[F[_]: Async]:
  protected val inventoryReservationRepository: InventoryReservationRepository[F]

  import JsonCodecs.given

  protected given createUserDecoder: EntityDecoder[F, CreateUserRequestDto] = jsonOf[F, CreateUserRequestDto]
  protected given loginUserDecoder: EntityDecoder[F, LoginUserRequestDto] = jsonOf[F, LoginUserRequestDto]
  protected given signupRequestDecoder: EntityDecoder[F, SignupRequestDto] = jsonOf[F, SignupRequestDto]
  protected given passwordLoginDecoder: EntityDecoder[F, PasswordLoginRequestDto] = jsonOf[F, PasswordLoginRequestDto]
  protected given managerPasswordLoginDecoder: EntityDecoder[F, ManagerPasswordLoginRequestDto] = jsonOf[F, ManagerPasswordLoginRequestDto]
  protected given createTravelerDecoder: EntityDecoder[F, CreateTravelerRequestDto] = jsonOf[F, CreateTravelerRequestDto]
  protected given createOrderDecoder: EntityDecoder[F, CreateOrderRequestDto] = jsonOf[F, CreateOrderRequestDto]
  protected given managerLoginDecoder: EntityDecoder[F, ManagerLoginRequestDto] = jsonOf[F, ManagerLoginRequestDto]
  protected given managerDecisionDecoder: EntityDecoder[F, ManagerDecisionRequestDto] = jsonOf[F, ManagerDecisionRequestDto]
  protected given managerBatchDecisionDecoder: EntityDecoder[F, ManagerBatchDecisionRequestDto] = jsonOf[F, ManagerBatchDecisionRequestDto]
  protected given registerAirlineManagerDecoder: EntityDecoder[F, RegisterAirlineManagerRequestDto] = jsonOf[F, RegisterAirlineManagerRequestDto]
  protected given registerHotelManagerDecoder: EntityDecoder[F, RegisterHotelManagerRequestDto] = jsonOf[F, RegisterHotelManagerRequestDto]
  protected given createManagerFlightDecoder: EntityDecoder[F, CreateManagerFlightRequestDto] = jsonOf[F, CreateManagerFlightRequestDto]
  protected given createManagerRoomTypeDecoder: EntityDecoder[F, CreateManagerRoomTypeRequestDto] = jsonOf[F, CreateManagerRoomTypeRequestDto]
  protected given registerRailwayManagerDecoder: EntityDecoder[F, RegisterRailwayManagerRequestDto] = jsonOf[F, RegisterRailwayManagerRequestDto]
  protected given trainAdminLoginDecoder: EntityDecoder[F, TrainAdminLoginRequestDto] = jsonOf[F, TrainAdminLoginRequestDto]
  protected given createTrainJourneyDecoder: EntityDecoder[F, CreateTrainJourneyRequestDto] = jsonOf[F, CreateTrainJourneyRequestDto]
  protected given registerAttractionManagerDecoder: EntityDecoder[F, RegisterAttractionManagerRequestDto] = jsonOf[F, RegisterAttractionManagerRequestDto]
  protected given attractionAdminLoginDecoder: EntityDecoder[F, AttractionAdminLoginRequestDto] = jsonOf[F, AttractionAdminLoginRequestDto]
  protected given createAttractionDecoder: EntityDecoder[F, CreateAttractionRequestDto] = jsonOf[F, CreateAttractionRequestDto]
  protected given createTicketTypeDecoder: EntityDecoder[F, CreateTicketTypeRequestDto] = jsonOf[F, CreateTicketTypeRequestDto]
  protected given createTicketEligibilityRuleDecoder: EntityDecoder[F, CreateTicketEligibilityRuleRequestDto] = jsonOf[F, CreateTicketEligibilityRuleRequestDto]
  protected given addAttractionItemDecoder: EntityDecoder[F, BookAttractionItemRequestDto] = jsonOf[F, BookAttractionItemRequestDto]
  protected given addFlightItemDecoder: EntityDecoder[F, BookFlightRequestDto] = jsonOf[F, BookFlightRequestDto]
  protected given addHotelItemDecoder: EntityDecoder[F, BookHotelRequestDto] = jsonOf[F, BookHotelRequestDto]
  protected given addTrainItemDecoder: EntityDecoder[F, BookTrainItemRequestDto] = jsonOf[F, BookTrainItemRequestDto]
  protected given authorizePaymentDecoder: EntityDecoder[F, PayOrderRequestDto] = jsonOf[F, PayOrderRequestDto]
  protected given requestRefundDecoder: EntityDecoder[F, RequestRefundRequestDto] = jsonOf[F, RequestRefundRequestDto]
  protected given createTourGroupDecoder: EntityDecoder[F, CreateTourGroupRequestDto] = jsonOf[F, CreateTourGroupRequestDto]
  protected given joinTourGroupDecoder: EntityDecoder[F, JoinTourGroupRequestDto] = jsonOf[F, JoinTourGroupRequestDto]
  protected given addMembershipTravelerDecoder: EntityDecoder[F, AddMembershipTravelerRequestDto] = jsonOf[F, AddMembershipTravelerRequestDto]
  protected given createGroupPlanItemDecoder: EntityDecoder[F, CreateGroupPlanItemRequestDto] = jsonOf[F, CreateGroupPlanItemRequestDto]
  protected given createGroupPlanOptionDecoder: EntityDecoder[F, CreateGroupPlanOptionRequestDto] = jsonOf[F, CreateGroupPlanOptionRequestDto]
  protected given createGroupPlanSelectionDecoder: EntityDecoder[F, CreateGroupPlanSelectionRequestDto] = jsonOf[F, CreateGroupPlanSelectionRequestDto]
  protected given submitGroupPlanSelectionDecoder: EntityDecoder[F, SubmitGroupPlanSelectionRequestDto] = jsonOf[F, SubmitGroupPlanSelectionRequestDto]
  protected given reviewGroupPlanSelectionDecoder: EntityDecoder[F, ReviewGroupPlanSelectionRequestDto] = jsonOf[F, ReviewGroupPlanSelectionRequestDto]
  protected given rejectGroupPlanSelectionDecoder: EntityDecoder[F, RejectGroupPlanSelectionRequestDto] = jsonOf[F, RejectGroupPlanSelectionRequestDto]
  protected given payGroupPlanSelectionDecoder: EntityDecoder[F, PayGroupPlanSelectionRequestDto] = jsonOf[F, PayGroupPlanSelectionRequestDto]
  protected given batchReviewGroupPlanSelectionsDecoder: EntityDecoder[F, BatchReviewGroupPlanSelectionsRequestDto] = jsonOf[F, BatchReviewGroupPlanSelectionsRequestDto]
  protected given batchRejectGroupPlanSelectionsDecoder: EntityDecoder[F, BatchRejectGroupPlanSelectionsRequestDto] = jsonOf[F, BatchRejectGroupPlanSelectionsRequestDto]
  protected given batchPayGroupPlanSelectionsDecoder: EntityDecoder[F, BatchPayGroupPlanSelectionsRequestDto] = jsonOf[F, BatchPayGroupPlanSelectionsRequestDto]
  protected given multipartDecoder: EntityDecoder[F, Multipart[F]] = EntityDecoder.multipart[F]

  protected object DepartureAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("departureAirport")
  protected object ArrivalAirportQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("arrivalAirport")
  protected object DepartureDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("date")
  protected object HotelLocationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("location")
  protected object AttractionCityQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("city")
  protected object FromStationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("fromStation")
  protected object ToStationQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("toStation")
  protected object CheckInDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkInDate")
  protected object CheckOutDateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("checkOutDate")
  protected object ManagerIdQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("managerId")
  protected object ManagerTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("managerType")
  protected object TaskStatusQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("status")
  protected object TaskResourceTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("resourceType")
  protected object SearchQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("q")
  protected object ExploreTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("type")
  protected object PaymentMethodQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("paymentMethod")
  protected object PaymentTokenQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("token")
  protected object LanguageQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("lang")

  protected def parseOptionalSearchText(searchTextValue: Option[String]): F[Option[String]] =
    searchTextValue match
      case Some(value) if value.trim.nonEmpty => Async[F].pure(Some(value.trim))
      case _                                  => Async[F].pure(None)

  protected def parseOptionalDate(dateValue: Option[String]): F[Option[LocalDate]] =
    dateValue match
      case Some(value) if value.trim.nonEmpty => Async[F].catchNonFatal(LocalDate.parse(value)).map(Some.apply)
      case _                                  => Async[F].pure(None)

  protected def parseOptionalStayPeriod(checkInDateValue: Option[String], checkOutDateValue: Option[String]): F[Option[StayPeriod]] =
    (checkInDateValue.map(_.trim).filter(_.nonEmpty), checkOutDateValue.map(_.trim).filter(_.nonEmpty)) match
      case (Some(checkInDateText), Some(checkOutDateText)) =>
        for
          checkInDate <- Async[F].catchNonFatal(LocalDate.parse(checkInDateText))
          checkOutDate <- Async[F].catchNonFatal(LocalDate.parse(checkOutDateText))
          stayPeriod <- fromEither(StayPeriod.create(checkInDate, checkOutDate))
        yield Some(stayPeriod)
      case (None, None) =>
        Async[F].pure(None)
      case _ =>
        Async[F].raiseError(SharedValidationError.RequiredFieldWasEmpty("stay-period"))

  protected def createTravelerEmergencyContact(createTravelerRequestDto: CreateTravelerRequestDto): F[Option[TravelerEmergencyContact]] =
    (createTravelerRequestDto.emergencyContactName, createTravelerRequestDto.emergencyContactPhoneNumber) match
      case (Some(contactName), Some(contactPhoneNumber)) =>
        for
          parsedContactName <- fromEither(PersonName.create(contactName))
          parsedContactNumber <- fromEither(ContactNumber.create(contactPhoneNumber))
        yield Some(TravelerEmergencyContact.create(parsedContactName, parsedContactNumber))
      case _ =>
        Async[F].pure(None)

  protected def parseRequestedSupplierReviewStatuses(taskStatusValue: Option[String]): Set[SupplierReviewStatus] =
    taskStatusValue.map(_.trim.toLowerCase).filter(_.nonEmpty) match
      case Some("confirmed") => Set(SupplierReviewStatus.SupplierConfirmed)
      case Some("rejected")  => Set(SupplierReviewStatus.SupplierRejected)
      case Some("all") =>
        Set(
          SupplierReviewStatus.PendingSupplierConfirmation,
          SupplierReviewStatus.SupplierConfirmed,
          SupplierReviewStatus.SupplierRejected
        )
      case _ => Set(SupplierReviewStatus.PendingSupplierConfirmation)

  protected def fromEither[A](value: Either[? <: Throwable, A]): F[A] =
    Async[F].fromEither(value.leftMap(identity))

  protected def toOrderResponseDto(order: Order): F[OrderResponseDto] =
    inventoryReservationRepository.findReservationsByOrderId(order.orderId).map(reservations => OrderResponseDto.fromDomain(order, reservations))
