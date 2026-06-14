// TrainErrors 定义 train 模块内部的领域错误类型，只在后端 planner / table / domain helper 中使用，不需要前端镜像。
package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

sealed trait TrainError extends DomainError:
  def message: String

object TrainError:
  final case class RailwayManagerWasNotFoundByEmail(primaryEmailAddress: EmailAddress) extends TrainError:
    override val message: String = s"Railway manager '${primaryEmailAddress.value}' was not found"

  final case class RailwayManagerWasNotFoundById(managerId: ManagerId) extends TrainError:
    override val message: String = s"Railway manager '${managerId.value}' was not found"

  final case class TrainWasNotFound(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' was not found"

  final case class TrainWasNotOpenForSale(trainId: TrainId, saleStartsAt: Instant, currentTime: Instant) extends TrainError:
    override val message: String = s"Train '${trainId.value}' is not on sale at $currentTime; sale starts at $saleStartsAt"

  final case class TrainWasClosed(trainId: TrainId, trainJourneyStatus: TrainJourneyStatus) extends TrainError:
    override val message: String = s"Train '${trainId.value}' is not bookable in status $trainJourneyStatus"

  final case class TrainHadTooFewStops(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' must contain at least two stops"

  final case class TrainStopSequenceWasInvalid(trainId: TrainId, stationCode: String) extends TrainError:
    override val message: String = s"Train '${trainId.value}' has an invalid stop sequence near station '$stationCode'"

  final case class TrainStopWasNotFound(trainId: TrainId, stationCode: TrainStationCode) extends TrainError:
    override val message: String = s"Train '${trainId.value}' does not contain station '${stationCode.value}'"

  final case class TrainStationOrderWasInvalid(trainId: TrainId, fromStationCode: TrainStationCode, toStationCode: TrainStationCode) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' requires from station '${fromStationCode.value}' to appear before '${toStationCode.value}'"

  final case class TrainSegmentPriceWasMissing(trainId: TrainId, seatClass: TrainSeatClass, fromStationCode: TrainStationCode, toStationCode: TrainStationCode) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' is missing a segment price for seat '${seatClass.value}' from '${fromStationCode.value}' to '${toStationCode.value}'"

  final case class TrainSeatInventoryWasNotFound(trainId: TrainId, seatClass: TrainSeatClass) extends TrainError:
    override val message: String = s"Train '${trainId.value}' does not have seat inventory '${seatClass.value}'"

  final case class TrainSeatInventoryWasNotBookable(trainId: TrainId, seatClass: TrainSeatClass, seatInventoryStatus: TrainSeatInventoryStatus) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' seat inventory '${seatClass.value}' is not bookable in status $seatInventoryStatus"

  final case class TrainTravelersWereEmpty(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' requires at least one traveler"

  final case class TrainTravelersContainedDuplicates(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' booking contains duplicate travelers"

  final case class TrainRefundPolicyDidNotMatch(trainId: TrainId, refundRequestedAt: Instant) extends TrainError:
    override val message: String = s"Train '${trainId.value}' has no refund policy matching refund request at $refundRequestedAt"

  final case class TrainSeatGenerationWasInvalid(trainId: TrainId, reason: String) extends TrainError:
    override val message: String = s"Train '${trainId.value}' seat generation was invalid: $reason"

  final case class TrainSeatAllocationWasNotAvailable(trainId: TrainId, seatClass: TrainSeatClass, requestedQuantity: Int) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' does not have enough available seats in '${seatClass.value}' for '$requestedQuantity' travelers"

  final case class TrainTravelerWasAlreadyBooked(trainId: TrainId, travelerId: TravelerId) extends TrainError:
    override val message: String = s"Traveler '${travelerId.value}' already has a ticket on train '${trainId.value}'"

  final case class TrainNumberConflict(trainNumber: TrainNumber, conflictingTrainId: TrainId) extends TrainError:
    override val message: String = s"Train number '${trainNumber.value}' conflicts with overlapping train '${conflictingTrainId.value}'"
