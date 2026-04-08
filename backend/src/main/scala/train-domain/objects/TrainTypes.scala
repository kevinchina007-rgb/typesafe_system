package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TrainJourneyStatus:
  case Draft, OnSale, Closed

enum TrainSeatInventoryStatus:
  case OpenForSale, SoldOut, Closed

enum TrainRefundType:
  case FullRefund, PartialRefund, NonRefundable

enum TrainSeatPositionType:
  case Window, Aisle, Middle, Other

enum TrainSeatStatus:
  case Available, Unavailable

enum TrainSeatPreference:
  case Window, Aisle, Middle, NoPreference

enum TrainError(val message: String) extends DomainError:
  case RailwayManagerWasNotFoundByEmail(primaryEmailAddress: EmailAddress)
      extends TrainError(s"Railway manager '${primaryEmailAddress.value}' was not found")
  case RailwayManagerWasNotFoundById(managerId: ManagerId)
      extends TrainError(s"Railway manager '${managerId.value}' was not found")
  case TrainWasNotFound(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' was not found")
  case TrainWasNotOpenForSale(trainId: TrainId, saleStartsAt: Instant, currentTime: Instant)
      extends TrainError(s"Train '${trainId.value}' is not on sale at $currentTime; sale starts at $saleStartsAt")
  case TrainWasClosed(trainId: TrainId, trainJourneyStatus: TrainJourneyStatus)
      extends TrainError(s"Train '${trainId.value}' is not bookable in status $trainJourneyStatus")
  case TrainHadTooFewStops(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' must contain at least two stops")
  case TrainStopSequenceWasInvalid(trainId: TrainId, stationCode: String)
      extends TrainError(s"Train '${trainId.value}' has an invalid stop sequence near station '$stationCode'")
  case TrainStopWasNotFound(trainId: TrainId, stationCode: TrainStationCode)
      extends TrainError(s"Train '${trainId.value}' does not contain station '${stationCode.value}'")
  case TrainStationOrderWasInvalid(trainId: TrainId, fromStationCode: TrainStationCode, toStationCode: TrainStationCode)
      extends TrainError(
        s"Train '${trainId.value}' requires from station '${fromStationCode.value}' to appear before '${toStationCode.value}'"
      )
  case TrainSegmentPriceWasMissing(trainId: TrainId, seatClass: TrainSeatClass, fromStationCode: TrainStationCode, toStationCode: TrainStationCode)
      extends TrainError(
        s"Train '${trainId.value}' is missing a segment price for seat '${seatClass.value}' from '${fromStationCode.value}' to '${toStationCode.value}'"
      )
  case TrainSeatInventoryWasNotFound(trainId: TrainId, seatClass: TrainSeatClass)
      extends TrainError(s"Train '${trainId.value}' does not have seat inventory '${seatClass.value}'")
  case TrainSeatInventoryWasNotBookable(trainId: TrainId, seatClass: TrainSeatClass, seatInventoryStatus: TrainSeatInventoryStatus)
      extends TrainError(
        s"Train '${trainId.value}' seat inventory '${seatClass.value}' is not bookable in status $seatInventoryStatus"
      )
  case TrainTravelersWereEmpty(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' requires at least one traveler")
  case TrainTravelersContainedDuplicates(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' booking contains duplicate travelers")
  case TrainRefundPolicyDidNotMatch(trainId: TrainId, refundRequestedAt: Instant)
      extends TrainError(s"Train '${trainId.value}' has no refund policy matching refund request at $refundRequestedAt")
  case TrainSeatGenerationWasInvalid(trainId: TrainId, reason: String)
      extends TrainError(s"Train '${trainId.value}' seat generation was invalid: $reason")
  case TrainSeatAllocationWasNotAvailable(trainId: TrainId, seatClass: TrainSeatClass, requestedQuantity: Int)
      extends TrainError(s"Train '${trainId.value}' does not have enough available seats in '${seatClass.value}' for '$requestedQuantity' travelers")
  case TrainTravelerWasAlreadyBooked(trainId: TrainId, travelerId: TravelerId)
      extends TrainError(s"Traveler '${travelerId.value}' already has a ticket on train '${trainId.value}'")
  case TrainNumberConflict(trainNumber: TrainNumber, conflictingTrainId: TrainId)
      extends TrainError(s"Train number '${trainNumber.value}' conflicts with overlapping train '${conflictingTrainId.value}'")

