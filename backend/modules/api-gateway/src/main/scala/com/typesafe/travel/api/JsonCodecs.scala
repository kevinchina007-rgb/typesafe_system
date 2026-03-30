package com.typesafe.travel.api

import com.typesafe.travel.api.dto.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

object JsonCodecs:
  given Encoder[HealthResponseDto] = deriveEncoder
  given Encoder[ErrorResponseDto] = deriveEncoder
  given Encoder[ApiErrorResponseDto] = deriveEncoder

  given Decoder[CreateUserRequestDto] = deriveDecoder
  given Decoder[LoginUserRequestDto] = deriveDecoder
  given Encoder[UserResponseDto] = deriveEncoder

  given Decoder[CreateTravelerRequestDto] = deriveDecoder
  given Encoder[TravelerResponseDto] = deriveEncoder
  given Encoder[TravelerListResponseDto] = deriveEncoder

  given Decoder[CreateOrderRequestDto] = deriveDecoder
  given Decoder[ManagerLoginRequestDto] = deriveDecoder
  given Decoder[RegisterAirlineManagerRequestDto] = deriveDecoder
  given Decoder[RegisterHotelManagerRequestDto] = deriveDecoder
  given Decoder[CreateManagerFlightRequestDto] = deriveDecoder
  given Decoder[CreateManagerRoomTypeRequestDto] = deriveDecoder
  given Decoder[RegisterRailwayManagerRequestDto] = deriveDecoder
  given Decoder[TrainAdminLoginRequestDto] = deriveDecoder
  given Decoder[RegisterAttractionManagerRequestDto] = deriveDecoder
  given Decoder[AttractionAdminLoginRequestDto] = deriveDecoder
  given Decoder[CreateAttractionRequestDto] = deriveDecoder
  given Decoder[CreateTicketTypeRequestDto] = deriveDecoder
  given Decoder[CreateTicketEligibilityRuleRequestDto] = deriveDecoder
  given Decoder[BookAttractionItemRequestDto] = deriveDecoder
  given Decoder[TrainStopRequestDto] = deriveDecoder
  given Decoder[TrainSeatInventoryRequestDto] = deriveDecoder
  given Decoder[TrainSegmentPriceRequestDto] = deriveDecoder
  given Decoder[TrainRefundPolicyRequestDto] = deriveDecoder
  given Decoder[CreateTrainJourneyRequestDto] = deriveDecoder
  given Decoder[BookTrainItemRequestDto] = deriveDecoder
  given Decoder[ManagerDecisionRequestDto] = deriveDecoder
  given Decoder[BookFlightRequestDto] = deriveDecoder
  given Decoder[BookHotelRequestDto] = deriveDecoder
  given Decoder[PayOrderRequestDto] = deriveDecoder
  given Decoder[RequestRefundRequestDto] = deriveDecoder
  given Decoder[CreateTourGroupRequestDto] = deriveDecoder
  given Decoder[JoinTourGroupRequestDto] = deriveDecoder
  given Decoder[AddMembershipTravelerRequestDto] = deriveDecoder
  given Decoder[CreateGroupPlanItemRequestDto] = deriveDecoder
  given Decoder[CreateGroupPlanOptionRequestDto] = deriveDecoder
  given Decoder[CreateGroupPlanSelectionRequestDto] = deriveDecoder
  given Decoder[SubmitGroupPlanSelectionRequestDto] = deriveDecoder
  given Decoder[ReviewGroupPlanSelectionRequestDto] = deriveDecoder
  given Decoder[RejectGroupPlanSelectionRequestDto] = deriveDecoder
  given Decoder[PayGroupPlanSelectionRequestDto] = deriveDecoder

  given Encoder[CabinInventoryResponseDto] = deriveEncoder
  given Encoder[FlightResponseDto] = deriveEncoder
  given Encoder[FlightListResponseDto] = deriveEncoder
  given Encoder[RoomTypeSummaryResponseDto] = deriveEncoder
  given Encoder[HotelResponseDto] = deriveEncoder
  given Encoder[HotelListResponseDto] = deriveEncoder
  given Encoder[TrainStopResponseDto] = deriveEncoder
  given Encoder[TrainSeatInventoryResponseDto] = deriveEncoder
  given Encoder[TrainSegmentPriceResponseDto] = deriveEncoder
  given Encoder[TrainRefundPolicyResponseDto] = deriveEncoder
  given Encoder[TrainResponseDto] = deriveEncoder
  given Encoder[TrainListResponseDto] = deriveEncoder
  given Encoder[TrainAdminSessionResponseDto] = deriveEncoder
  given Encoder[AttractionTicketTypeRuleResponseDto] = deriveEncoder
  given Encoder[AttractionTicketTypeResponseDto] = deriveEncoder
  given Encoder[AttractionResponseDto] = deriveEncoder
  given Encoder[AttractionListResponseDto] = deriveEncoder
  given Encoder[AttractionAdminSessionResponseDto] = deriveEncoder
  given Encoder[FlightItemDetailsResponseDto] = deriveEncoder
  given Encoder[HotelItemDetailsResponseDto] = deriveEncoder
  given Encoder[TrainItemDetailsResponseDto] = deriveEncoder
  given Encoder[AttractionItemDetailsResponseDto] = deriveEncoder
  given Encoder[ManagerSessionResponseDto] = deriveEncoder
  given Encoder[SupplierReviewDecisionResponseDto] = deriveEncoder
  given Encoder[ManagerBookingTaskResponseDto] = deriveEncoder
  given Encoder[ManagerBookingTaskListResponseDto] = deriveEncoder
  given Encoder[ManagerRefundTaskResponseDto] = deriveEncoder
  given Encoder[ManagerRefundTaskListResponseDto] = deriveEncoder
  given Encoder[OrderLineItemResponseDto] = deriveEncoder
  given Encoder[PaymentResponseDto] = deriveEncoder
  given Encoder[RefundResponseDto] = deriveEncoder
  given Encoder[OrderResponseDto] = deriveEncoder
  given Encoder[OrderListResponseDto] = deriveEncoder
  given Encoder[TourGroupSummaryResponseDto] = deriveEncoder
  given Encoder[TourGroupMembershipResponseDto] = deriveEncoder
  given Encoder[TourGroupMembershipTravelerResponseDto] = deriveEncoder
  given Encoder[GroupPlanItemResponseDto] = deriveEncoder
  given Encoder[GroupPlanOptionResponseDto] = deriveEncoder
  given Encoder[GroupPlanSelectionResponseDto] = deriveEncoder
  given Encoder[GroupSelectionOrderLinkResponseDto] = deriveEncoder
  given Encoder[TourGroupDetailsResponseDto] = deriveEncoder
  given Encoder[TourGroupListResponseDto] = deriveEncoder
